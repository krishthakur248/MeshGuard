# MeshGuard – Nearby Connections & Permission Lifecycle Post-Mortem

This document summarizes the root causes and architectural solutions for the discovery and permission bugs encountered during Step 3 (Permissions) and Step 4 (Discovery) of MeshGuard.

---

## 1. Bug: "Devices Only Detect Each Other on First Launch After Clearing App Data"

### Symptoms
- When clearing app data on both phones, entering MeshGuard detects the other device.
- After closing and reopening the app (without clearing data), peer count remains 0 ("No nearby detection").
- Logcat shows no `NearbyMesh` logs on reopen, even though Bluetooth is `ON`.

### Root Causes
1. **Unpersisted Mesh State:**
   - In `NearbyMeshRepository`, `_isBroadcastingBeacon` was an in-memory `MutableStateFlow(false)`.
   - On the first open after clearing data, the user passed through `PermissionsScreen`, which called `toggleBroadcast(true)`.
   - On subsequent launches, onboarding was already finished, so `MainActivity` navigated straight to `Screen.Home` or `Screen.ResponderDashboard`. `PermissionsScreen` was bypassed, and nothing ever started the mesh.
2. **Survivor Role Was Excluded:**
   - In `PermissionsScreen.kt`, `toggleBroadcast(true)` was guarded by:
     ```kotlin
     if (AppStateManager.userRole.value == UserRole.RESCUER) {
         AppDependencies.meshRepository.toggleBroadcast(true)
     }
     ```
     This left `SURVIVOR` accounts without active broadcasting unless they manually found and tapped the "RESUME" button.
3. **Endpoint Discovery Caching in Google Play Services:**
   - In Google Nearby Connections (`EndpointDiscoveryCallback.onEndpointFound`), an endpoint is only reported **once** when found.
   - If a peer disconnected or closed the app, Google Play Services cached that endpoint ID. Upon reconnecting, `onEndpointFound` was not called again unless discovery was explicitly stopped and restarted.
4. **Tiebreaker Deadlock:**
   - To prevent connection collisions in `P2P_CLUSTER`, each device appended an 8-character UUID to its advertised name (`Model#uuid`).
   - The device with the smaller UUID was told to `WAIT` for the larger UUID to call `requestConnection`.
   - If the device with the larger UUID was delayed in scanning or dropped a BLE advertisement packet, the device with the smaller UUID would wait indefinitely, causing a complete stall.
5. **BLE Radio Race Conditions on Quick Restart:**
   - Calling `startAdvertising()` and `startDiscovery()` immediately on the next line after `stopAdvertising()` / `stopDiscovery()` overwhelmed the Bluetooth HAL, resulting in `STATUS_ALREADY_ADVERTISING` (8001) or `STATUS_ERROR` (13).

### Solutions Implemented
- **Persistent Mesh Preference ([AccountRepository.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/data/repository/AccountRepository.kt)):**
  - Added `KEY_IS_BROADCASTING` to `SharedPreferences` (defaults to `true`).
  - `NearbyMeshRepository` loads this state on creation and persists updates whenever `toggleBroadcast` is called.
- **Auto-Start in `MainActivity.onResume()` ([MainActivity.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/MainActivity.kt)):**
  - When returning to the foreground with permissions and radios (BT + Location) active, `MainActivity` verifies that the mesh is running if `loadIsBroadcasting()` is `true`.
- **Unconditional Start on Permission Completion ([PermissionsScreen.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/ui/screens/onboarding/PermissionsScreen.kt)):**
  - Removed the `userRole == RESCUER` check so both Survivors and Rescuers start advertising and scanning when entering MeshGuard.
- **Auto-Rescan on Disconnect ([NearbyMeshRepository.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/data/repository/NearbyMeshRepository.kt)):**
  - Added `rescan()` to the `MeshRepository` interface.
  - When `onDisconnected` triggers, `rescan()` restarts discovery to flush the Play Services endpoint cache and enable immediate reconnection.
- **Tiebreaker Fallback Timeout ([NearbyMeshRepository.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/data/repository/NearbyMeshRepository.kt)):**
  - The device with the smaller UUID waits up to 2.5 seconds for an incoming connection request. If none arrives, it initiates the connection itself, breaking any deadlock.
- **Radio Settle Delay ([NearbyMeshRepository.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/data/repository/NearbyMeshRepository.kt)):**
  - `startMesh()` calls `stopAdvertising()`, `stopDiscovery()`, and `stopAllEndpoints()`, then introduces a 250ms coroutine delay before issuing `startAdvertising()` and `startDiscovery()`.
  - `STATUS_ALREADY_ADVERTISING` (8001) and `STATUS_ALREADY_DISCOVERING` (8002) are caught and handled as non-fatal successes.

---

## 2. Bug: "Permissions Screen Flashed Away with Bluetooth Turned Off"

### Symptoms
- When opening the app with Bluetooth disabled in phone settings, the Permissions screen appeared for a split second and immediately disappeared, dumping the user into the main screens.

### Root Causes
1. **Premature Approval in `checkAndInitializePermissions()`:**
   - In `PermissionsViewModel`, `checkAndInitializePermissions()` was called from a `LaunchedEffect` as soon as `PermissionsScreen` composed.
   - Because OS-level permissions (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `ACCESS_FINE_LOCATION`) were already granted by Android, `allEssentialGranted` evaluated to `true`.
   - The method directly called `AppStateManager.setPermissionsGranted(true)`, which instantly flipped `startDestination` back to `Home` before the user could even interact with the screen.
2. **Missing Runtime Location Check on Android 12+:**
   - Android 12+ (API 31+) uses `BLUETOOTH_SCAN` and `BLUETOOTH_ADVERTISE`.
   - However, Google Nearby Connections **strictly requires `ACCESS_FINE_LOCATION` on all Android versions** (including Android 12, 13, 14, 15). Omitting location permission causes Nearby Connections to silently drop discovery.

### Solutions Implemented
- **Decoupled UI State from Permission Approval ([PermissionsViewModel.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/ui/viewmodel/PermissionsViewModel.kt)):**
  - `checkAndInitializePermissions()` only updates the visual cards on the screen.
  - `AppStateManager.setPermissionsGranted(true)` is **only** called inside `onCompletePermissions()`, which executes only when the user taps "ENTER MESHGUARD" and both hardware radios (Bluetooth and Location) are verified to be `ON`.
- **Reactive Navigation Guard ([MainActivity.kt](file:///c:/Users/Asus/Desktop/AndroidStudioProjects/MeshGuard/app/src/main/java/com/example/meshguard/MainActivity.kt)):**
  - Added a reactive `LaunchedEffect(hasGrantedPermissions)` in `setContent` that immediately forces navigation back to `Screen.Permissions.route` if hardware is turned off during runtime.

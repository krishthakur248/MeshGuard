# MeshGuard – Architecture

## Stack
- Android only, Kotlin, Jetpack Compose, minSdk 26
- Device-to-device: Google Nearby Connections, Strategy.P2P_CLUSTER
- Local database: Room (SQLite) = the "gossip buffer", plus a small local store for the account, role and sending on/off state
- Location: Fused Location Provider (GPS works without internet)
- Rescuer map: offline-capable (osmdroid with cached tiles), never needs internet at runtime

## Roles and visibility
- Role (SURVIVOR or RESCUER) is chosen at signup and stored locally. The app routes to screens by role.
- Rescuer-only screens (list, details, map) must not be reachable from survivor mode.
- Survivor phones still store and forward everyone's packets, but never display them.
- Note for later: hiding data in the UI does not stop someone reading the phone's storage.
  Encrypting personal info for rescuer devices only (security section) is required before any real-world use.

## Background operation
- Use a foreground service with a visible notification, started when the survivor presses Start/Resume.
- The service owns Nearby Connections (advertise + discover), location updates and packet sending, so it keeps running with the app minimised or the screen off.
- Declare the correct foreground service types (location and connectedDevice) and the matching permissions for Android 14+.
- Handle permissions for each Android version (Nearby/Bluetooth, location, notifications); prefer the approach that does not need always-on background location if possible.
- Pause/Stop stops the service. Remember the on/off state across app restarts.
- Default: refresh location every 30 seconds (adjustable). Send a new packet immediately when the status changes.

## Packet format (JSON)
```
{
  "packetId": "uuid",
  "survivorId": "uuid",
  "version": 12,              // increases with every new packet; used only to break timestamp ties
  "statusTag": "UNKNOWN",     // UNKNOWN | TRAPPED | INJURED | NEEDS_MEDICATION | SAFE
  "priority": 3,              // from statusTag; default TRAPPED=1, INJURED=2, NEEDS_MEDICATION=2, UNKNOWN=3, SAFE=4
  "userInfo": { "name": "", "age": 0, "bloodGroup": "", "medicalNotes": "", "emergencyContact": "" },
  "location": { "lat": 0.0, "lon": 0.0, "accuracyMeters": 0, "capturedAt": 1730000000000 },
  "timestamp": 1730000000000,  // when the survivor's phone created this packet (ms since 1970); newest wins
  "hopCount": 2,
  "ttl": 10,                  // drop when hopCount >= ttl
  "historyNodes": []          // Phase 2: breadcrumb
}
```
Chat messages use a separate packet type "CHAT" with text, ttl and hopCount.

## Sync rules (newest timestamp wins)
Each survivor's record is written only by that survivor's phone. Every phone, survivor or rescuer, keeps ONLY the newest packet per survivorId.
- Compare `timestamp`: a packet with a newer timestamp replaces the stored one; an older or equal one is discarded. If the timestamps are equal, the higher `version` wins.
- Delete the older packet from the database when it is replaced, so the rescuer list and map show exactly one entry per survivor.
- Timestamp source: use the GPS fix time (accurate and works offline) when a location fix exists, otherwise the phone clock.
- Clock safety: if a packet's timestamp is more than 10 minutes ahead of the receiving phone's own clock, do not let it replace a stored packet (default, adjustable). This stops a wrong clock from blocking real updates.
- On connection: both phones send a summary (survivorId + timestamp), then send only what the other is missing or has an older timestamp for.
- Send order: lowest priority number first, then newest.
- Drop packets when hopCount >= ttl.
- Buffer cap (default 500). When full, evict lowest priority first, then oldest.

## Security (Phase 2)
- Encrypt userInfo and medical data with a random AES-256-GCM key per record.
- Encrypt that key with the rescue authority's PUBLIC key (embedded in the app). Only rescuer devices hold the private key.
- Sign packets so fake SOS packets can be rejected.
- Rescuer access code is demo-only; a real system needs proper rescuer key provisioning.

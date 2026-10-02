# MeshGuard – Build steps
Follow in order. Only start the next step when the current one works on a real phone.
Test with real Android phones (emulators cannot test Bluetooth / Wi-Fi Direct).
UI is frozen (see AGENTS.md): every step connects logic to the existing UI; it does not redesign it.

1. **Integrate the existing UI** – move the Kotlin UI from design/ into the app module, fix imports/dependencies, set up navigation so every screen opens. Sample data only, no logic.
   Done when: app builds and all screens open on the phone.
2. **Local account and roles** – signup/profile saved locally, role choice (rescuer needs access code), screens routed by role.
   Done when: a survivor account never reaches list/map screens; a rescuer account can.
3. **Permissions** – Nearby/Bluetooth, location, notifications, correct per Android version.
   Done when: everything granted and no crash.
4. **Discovery** – advertise + discover with Nearby Connections. Done when: two phones show each other.
5. **Send one packet** – send a hard-coded packet from phone A to phone B. Done when: B receives it.
6. **Storage** – Room database + packet model. Done when: received packets survive an app restart.
7. **Gossip relay** – timestamp-based sync (newest packet per survivor wins, older ones are discarded), dedupe, ttl/hopCount, priority order, buffer cap.
   Done when: with 3 phones, A -> B -> C works even if A and C never meet. Also: if two packets from the same survivor arrive, only the one with the newer timestamp is kept.
8. **Survivor broadcast** – status picker (default UNKNOWN), Start/Resume/Pause, real location + profile in packets.
   Done when: a rescuer phone receives a survivor packet with the right status and location.
9. **Background service** – foreground service keeps sending and relaying with the app minimised and the screen off.
   Done when: it keeps working for 10+ minutes in the background.
10. **Rescuer list** – real received packets, sorted, filters, detail view.
11. **Rescuer map** – offline-capable map with a marker per survivor, coloured by status.
12. **Chat** – text messages relayed the same way.
13. Phase 2: encryption, signed packets, rescuer actions, breadcrumbs, heat map (one at a time).

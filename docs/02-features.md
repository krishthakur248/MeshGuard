# MeshGuard – Features and scope
Read docs/00-core-idea.md first. Build the MVP only until told otherwise.

## MVP (build now)
1. **Local account + role** – Survivor or Rescuer (rescuer needs the access code). App opens the right screens for the role.
2. **Survivor broadcast** – profile, status picker (default UNKNOWN), Start/Resume and Pause buttons,
   packet = user info + location + status.
3. **Background operation** – sending and relaying continue with the app in the background / screen off.
4. **Auto discovery and relay (epidemic routing)** – every phone, in either role, finds nearby phones
   and exchanges stored packets. Survivor phones relay silently.
5. **Priority sync** – urgent packets (Trapped, Injured) are exchanged first.
6. **Rescuer list** – all received packets, sorted by priority then newest, with filters and a detail view.
7. **Rescuer map** – one marker per survivor, coloured by status, works offline.
8. **Offline gossip chat** – short text messages relayed toward rescuers (only after 1-7 work).

## Phase 2 (after the MVP works on 3 phones)
9. **Encryption of personal data** – survivor info and medical data readable only by rescuer devices.
10. **Signed packets** – reject fake SOS packets.
11. **Rescuer actions** – mark a survivor as handled / rescued.
12. **Breadcrumb / chain of contact** – which devices a packet passed through.
13. **Heat map / clustering** on the rescuer map.

## Later / optional
14. Voice-drop relay (short, low priority, Wi-Fi Direct only).
15. iOS (not planned; cannot talk to Android over Nearby Connections).

# MeshGuard – CORE IDEA (highest priority)
If any other file conflicts with this one, THIS FILE WINS.

MeshGuard has TWO types of users. The whole app is built around this split.

## 1. Survivor (normal user account)
- Creates a local account/profile: name, age, blood group, medical notes, emergency contact.
- Has a status. Default is UNKNOWN. Choices: UNKNOWN, TRAPPED, INJURED, NEEDS_MEDICATION, SAFE.
- Starts sending by pressing the Start / Resume button. A Pause / Stop button stops it.
  (The on/off state is remembered if the app is closed.)
- While sending is ON, the app keeps working in the BACKGROUND (screen off, app minimised).
  It keeps sending packets that contain: user info + current location + selected status.
  A new packet is sent immediately when the status changes, and the location is refreshed regularly.
- The survivor's phone also silently relays other people's packets (data mule) in the background.
  Those packets are stored and forwarded but NEVER shown to the survivor.
- A survivor CANNOT see: the received-packets list, the map, or anyone else's data.
  A survivor only has: profile, status selection, Start/Resume/Pause, and a simple sending indicator.

## 2. Rescuer
- Same networking as everyone (also receives and relays packets).
- Sees ALL received packets:
  - a LIST (sorted by priority then newest, with filters and a detail view for each person), and
  - a MAP with one marker per survivor (colour by status).
- The list and the map exist ONLY in rescuer mode. Rescuers have many more options than survivors
  (filters, detail view, marking someone as handled, and later decrypting medical data).
- To become a rescuer the user enters a rescuer access code at signup.
  For the demo this is a fixed code stored in app config (no server). Mark it clearly as demo-only.

## Timestamps and the latest-packet rule
- Every packet carries a `timestamp` (when the survivor's phone created it).
- Every phone, survivor or rescuer, keeps ONLY the newest packet per survivor. When a packet for the same
  survivor arrives, the one with the newer timestamp is kept and the older one is discarded.
- So the rescuer list and map show exactly ONE entry per survivor: their latest.
- Timestamp source: the GPS fix time when available (accurate with no internet), otherwise the phone clock.

## Always true
- Everything works with NO internet and NO server.
- Background sending is a core feature, not an extra.
- The rescuer map must work without internet. Use an offline-capable map (for example osmdroid
  with pre-downloaded tiles). If tiles are missing, still show the markers on a plain background.
  Do not use Google Maps (needs API key and internet) without asking me first.
- The UI is already built. Do not change it (see AGENTS.md, "UI is frozen").

## Assumptions (tell me if any are wrong)
- Survivor starts as UNKNOWN and begins broadcasting when pressing Start/Resume.
- Accounts are local to the phone (no login server).

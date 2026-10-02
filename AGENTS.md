# MeshGuard – agent instructions (read this at the start of EVERY task)

You are helping a student build MeshGuard, an offline Android disaster-rescue app.
The student is new to Android, so explain simply and work in small steps.

## Where to find things
- docs/00-core-idea.md     -> READ FIRST. The two user types (Survivor / Rescuer). Highest priority.
- docs/01-overview.md      -> problem, solution, impact
- docs/02-features.md      -> what to build (MVP) and what NOT to build yet
- docs/03-architecture.md  -> tech stack, packet format, sync, background and security rules
- docs/04-build-steps.md   -> ordered build steps (follow this order)
- design/                  -> the existing finished UI in Kotlin / Jetpack Compose

## HARD RULES (never break these, in any step)
1. UI IS FROZEN. The UI in design/ (and its copy in app/src/main/java/.../ui/) is finished and approved.
   - Do NOT change how it looks: layout, colours, fonts, spacing, text, icons, animations, navigation structure.
   - Do NOT delete or rename existing screens or composables.
   - You may only: replace sample data with real state, add parameters / click handlers, and fix compile errors.
   - Tell me every UI file you touched and exactly what you changed.
   - If a feature needs a screen or element that does not exist, STOP and tell me what is missing. Do not invent it.
     If I say yes, copy the style of the existing screens exactly.
2. KEEP THE CORE IDEA. Two roles, Survivor and Rescuer, exactly as in docs/00-core-idea.md.
   - Only a Rescuer can ever see the received-packets list or the map.
   - A Survivor never sees other people's data, but their phone still relays it in the background.
3. Background sending must really work (foreground service) when the survivor has pressed Start/Resume.
4. Everything works offline. No internet, no server, no cloud services, no API keys without asking me.
5. Android only. Kotlin + Jetpack Compose. minSdk 26.

## Working rules
- Build ONLY the step I ask for. Do not jump ahead or add features that are not in docs/02-features.md.
- Networking: Google Nearby Connections API, Strategy P2P_CLUSTER. Storage: Room.
- Keep UI separate from logic: composables only show state and call functions. Logic lives in ViewModels / services / repositories.
- Handle runtime permissions correctly for Android 12+ and older versions (Nearby/Bluetooth, location, notifications).
- Before big changes, give a short plan. After every step tell me: (1) how to run it, (2) what I should see, (3) how to test with 2 or 3 phones.
- If a build error happens, explain the cause in one or two plain sentences, then fix it.
- Keep code simple and readable, with short comments on non-obvious parts.

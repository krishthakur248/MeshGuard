---
name: Resilience Tactical
colors:
  surface: '#111318'
  surface-dim: '#111318'
  surface-bright: '#37393e'
  surface-container-lowest: '#0c0e12'
  surface-container-low: '#1a1c20'
  surface-container: '#1e2024'
  surface-container-high: '#282a2e'
  surface-container-highest: '#333539'
  on-surface: '#e2e2e8'
  on-surface-variant: '#bbc9cf'
  inverse-surface: '#e2e2e8'
  inverse-on-surface: '#2f3035'
  outline: '#859399'
  outline-variant: '#3c494e'
  surface-tint: '#47d6ff'
  primary: '#a5e7ff'
  on-primary: '#003543'
  primary-container: '#00d2ff'
  on-primary-container: '#00566a'
  inverse-primary: '#00677f'
  secondary: '#ffb4aa'
  on-secondary: '#690003'
  secondary-container: '#c5020b'
  on-secondary-container: '#ffd2cc'
  tertiary: '#ffd5b1'
  on-tertiary: '#4b2800'
  tertiary-container: '#ffb062'
  on-tertiary-container: '#764200'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#b6ebff'
  primary-fixed-dim: '#47d6ff'
  on-primary-fixed: '#001f28'
  on-primary-fixed-variant: '#004e60'
  secondary-fixed: '#ffdad5'
  secondary-fixed-dim: '#ffb4aa'
  on-secondary-fixed: '#410001'
  on-secondary-fixed-variant: '#930005'
  tertiary-fixed: '#ffdcbf'
  tertiary-fixed-dim: '#ffb874'
  on-tertiary-fixed: '#2d1600'
  on-tertiary-fixed-variant: '#6a3b00'
  background: '#111318'
  on-background: '#e2e2e8'
  surface-variant: '#333539'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Space Grotesk
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Work Sans
    fontSize: 17px
    fontWeight: '500'
    lineHeight: 26px
  body-md:
    fontFamily: Work Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Work Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.04em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.06em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1rem
  margin-mobile: 0.75rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1.25rem
  space-xl: 2rem
---

## Brand & Style

The design system is engineered for life-or-death crisis intervention, off-grid survival tracking, and ad-hoc mesh networking. Targeted at disaster survivors, search-and-rescue teams, and community first-responders, the interface prioritizes extreme glanceability, high stress tolerance, and zero battery waste.

The design movement combines **High-Contrast Tactical Utility** with **Tactile OLED Minimalism**:
- **Absolute legibility under direct sunlight or pitch-black darkness:** Pure OLED dark grounding paired with piercing, purposeful signal colors.
- **Physical immediacy:** UI controls feel like tactile, ruggedized hardware gear—thick tap affordances, definite borders, and zero ornamental ambiguity.
- **Calm, decisive clarity:** Motion is reduced to instant micro-transitions to conserve hardware resources and preserve battery life during extended grid collapse.

## Colors

The color palette is strictly functional, mapping directly to triage protocols, mesh network integrity, and resource allocation:

- **Mesh / Telemetry Cyan (`#00D2FF`):** Primary interactive color, signaling active peer-to-peer radio pings, decentralized network connectivity, and fresh survival data.
- **Critical / Threat Red (`#FF3B30`):** SOS triggers, active hazards, entrapment alerts, and low battery thresholds.
- **Emergency Warning Orange (`#FF9500`):** Severe weather, impending danger, structural risk, and missing survivor signals.
- **Urgent Resources Yellow (`#FFCC00`):** Critical triage tier-2, urgent medical needs, and localized heat maps.
- **Safe / Resolved Green (`#34C759`):** Safe survivor status, verified extraction zones, full supplies, and confirmed delivery nodes.
- **Surface Architecture:** Grounded on OLED True Pitch `#0A0C10`, stepped into elevated tactical slate surfaces `#141820` (Surface Base) and `#1E232E` (Surface Raised/Card). High-contrast border delineations use `#2D3545` to prevent visual blur without lighting up extra OLED pixels.

## Typography

The typography system is split into three functional roles to prevent cognitive fatigue under adrenaline:

- **Display & Headlines (Space Grotesk):** Geometric, industrial, and assertive. Provides immediate situational orientation with distinct mechanical glyphs that avoid letter confusion in low-light environments.
- **Body & Triage Narratives (Work Sans):** Highly legible grotesque sans-serif with open apertures and distinct letterforms, preventing eye strain during dense survivor reports and survival instructions.
- **Labels, Telemetry & Status Badges (JetBrains Mono):** Monospaced precision for GPS coordinates, BLE signal strength (RSSI), battery percentages, hop counts, and survivor status timestamps.

## Layout & Spacing

A compact, edge-conscious fluid grid system tailored for one-handed operation on ruggedized Android hardware:

- **Compact Grid Rhythm:** Standard 4-column layout on mobile, scaling to 8 columns on tactical field tablets. Margins stay tight (`0.75rem` / `12px` on compact screens) to preserve maximal horizontal space for coordinates, maps, and status streams.
- **Emergency Touch Targets:** Interactive targets never drop below a hard minimum of 48×48dp; primary actions (SOS, Broadcast, Check-In) feature minimum heights of 56dp to accommodate wet, cold, or gloved hands.
- **Persistent Offline Horizon:** Screen layouts reserve a dedicated 40dp top band below the system status bar for hardware mesh status, hop count, and direct peer radio reach.

## Elevation & Depth

To maximize battery life on OLED displays and eliminate distracting artifacts under high glare, visual hierarchy relies entirely on **Tonal Layering** and **Crisp Structural Outlines** rather than diffused drop shadows:

- **Ground Zero (Base Layer - `#0A0C10`):** Pitch-black foundation that switches off OLED subpixels.
- **Level 1 Containers (`#141820`):** Main operational surfaces, map card overlays, and message lists with a 1px border of `#232B3A`.
- **Level 2 Containers (`#1E232E`):** Active interactive cards, focused inputs, and persistent modal trays with a 1px border of `#2D3545`.
- **Alert Elevation Overrides:** Critical alerts break standard elevation using an interior 2px high-visibility border in their respective diagnostic color (`#FF3B30` for SOS, `#FF9500` for Warning) paired with a 10% translucent color fill over `#141820`. No blurry drop shadows are permitted.

## Shapes

The design uses a tight, structured shape language (`roundedness: 1`) evoking physical field gear and rugged tactical devices:

- **Core Radius (`0.25rem` / `4px`):** Used for micro-badges, telemetry pills, and status tags to retain a sharp, dense data footprint.
- **Component Radius (`0.5rem` / `8px`):** Used for standard buttons, input fields, tactical lists, and cards.
- **SOS Action Exception (`0.75rem` / `12px`):** The primary distress button utilizes slightly softer geometry to distinguish it instantly from analytical cards and technical rows.

## Components

### Buttons
- **Critical SOS Button:** Full-width, minimum height 64dp, background `#FF3B30`, text `#0A0C10` (Space Grotesk Bold, 18px uppercase), border none. Supports a physical press-and-hold radial progress fill for accidental trigger prevention.
- **Mesh Primary Button:** Background `#00D2FF`, text `#0A0C10` (Space Grotesk Bold, 16px), 52dp height, 8px radius.
- **Tactical Secondary Button:** Background `#1E232E`, text `#FFFFFF`, 1px border `#2D3545`, 52dp height.

### Offline Mesh Status Bar (Header Component)
- Persistent top strip anchored directly beneath the Android status bar. Height 40dp, background `#141820`, bottom border 1px `#232B3A`.
- Left-aligned indicator displaying radio mode (LoRa / BLE / P2P Wi-Fi) with dynamic dot states: pulsing Cyan (`#00D2FF`) for transmitting, solid Green (`#34C759`) for linked, and Amber (`#FF9500`) for disconnected mesh.
- Right-aligned monospaced metrics displaying node count (`NODES: 14`) and local battery reserve.

### Survivor & Resource Cards
- Built on `#141820` with an 8px radius and 1px border `#232B3A`.
- Left triage indicator: A continuous 4px vertical bar keyed to survivor status (`#FF3B30` Critical, `#FFCC00` Meds, `#34C759` Safe).
- Title set in Space Grotesk 18px; timestamps, RSSI signal levels, and GPS coordinates set in JetBrains Mono 12px muted text (`#8A95A5`).

### Chips & Telemetry Badges
- 4px radius, compact padding (4px vertical, 8px horizontal).
- JetBrains Mono 11px uppercase.
- Neutral state: Background `#1E232E`, text `#C4CBD4`.
- Active hazard state: 10% translucent red fill, 1px border `#FF3B30`, text `#FF3B30`.

### Form Controls (Checkboxes, Radios, Inputs)
- **Checkboxes & Radios:** Rigid 24×24dp squares/circles with high-visibility 2px borders (`#3E485C`). Selected state fills with `#00D2FF` carrying a contrasting `#0A0C10` checkmark.
- **Input Fields:** 52dp height, background `#141820`, border 1px `#2D3545`. Active focus shifts border to 2px `#00D2FF` with zero ambient glow. Text in Work Sans 16px `#FFFFFF` with placeholder `#5A6577`.

### Tactical Lists
- Separated by 1px rules `#1E232E`, 0px padding between card items to maintain dense, scannable data density during search operations. Tap targets remain at 56dp per row.
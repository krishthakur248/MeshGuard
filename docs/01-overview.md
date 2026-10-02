# MeshGuard – Overview
Decentralized Disaster Resilience & Survivor Tracking
Theme: Disaster Management / Cybersecurity

## Problem
In natural disasters (earthquakes, floods, etc.) cellular networks, power grids and internet
are usually the first things to fail. Rescue relies on centralized communication, so trapped
survivors are isolated and rescue teams are blind to the situation on the ground. There is no
"offline-first" system that lets survivors signal for help or send vital medical data to
responders without existing infrastructure.

## Solution: gossip-protocol mesh
MeshGuard turns every smartphone into a "Data Mule". It uses an opportunistic mesh network
(Bluetooth / Wi-Fi Direct through Nearby Connections) to build a human-to-human chain. Data
"hops" from device to device (gossip / epidemic routing) until it reaches a rescue worker,
with no cell tower or internet.

Example: Survivor A meets Survivor B, B later meets a Rescue Worker, so the worker receives A's data.

## What makes it different
- Infrastructure-independent: works in blackout zones.
- No central server: the network gets stronger as more people enter the zone.
- Human-centric: the crowd becomes a decentralized sensor network, giving rescuers a
  "map of need" instead of isolated distress calls.

## Impact
- Faster response: critical medical needs in a sector are known before rescuers arrive.
- Resource optimization: "last seen" proximity data stops rescuers searching empty areas.
- Life-saving: medical history can reach doctors even if the patient is unconscious.

## Honest limits (keep these in mind, do not over-claim in the UI or text)
- The mesh only works if phones physically pass near each other. Rescuers walking the area
  act as data mules.
- Proximity (Bluetooth signal strength) is approximate. It narrows the search area; it does
  not give exact positions.

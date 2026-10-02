package com.example.meshguard.data.model

/**
 * Triage urgency status levels for survivor packets.
 * Step 8: added UNKNOWN as default status (priority 3) per docs/00-core-idea.md.
 */
enum class UrgencyStatus(val label: String, val priorityLevel: Int) {
    TRAPPED("Trapped", 1),
    INJURED("Injured", 2),
    NEEDS_INSULIN("Needs Meds", 2),
    UNKNOWN("Unknown", 3),
    NEED_WATER("Need Water", 4),
    SAFE("Safe", 5)
}

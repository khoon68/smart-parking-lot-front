package com.example.parkingapp.ui.theme

fun formatContinuousTime(slots: List<String>): String {
    if (slots.isEmpty()) return ""
    val sorted = slots.sorted()
    val start = sorted.first().substringBefore("~")
    val end = sorted.last().substringAfter("~")
    return "$start~$end"
}

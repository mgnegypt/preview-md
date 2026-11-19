package com.mgnegypt.previewmd.util

/** Buckets used by the localized relative-time labels on file cards. */
enum class RelativeBucket {
    NOW,
    MINUTES,
    HOURS,
    YESTERDAY,
    DAYS
}

data class RelativeTime(
    val bucket: RelativeBucket,
    val count: Int
)

/**
 * Maps a past timestamp to a display bucket: under a minute is "now",
 * then minutes, hours, yesterday (24-48h), then days. Pure and tested.
 */
fun relativeTime(nowMillis: Long, thenMillis: Long): RelativeTime {
    val diff = (nowMillis - thenMillis).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = minutes / 60L
    return when {
        minutes < 1 -> RelativeTime(RelativeBucket.NOW, 0)
        minutes < 60 -> RelativeTime(RelativeBucket.MINUTES, minutes.toInt())
        hours < 24 -> RelativeTime(RelativeBucket.HOURS, hours.toInt())
        hours < 48 -> RelativeTime(RelativeBucket.YESTERDAY, 1)
        else -> RelativeTime(RelativeBucket.DAYS, (hours / 24L).toInt())
    }
}

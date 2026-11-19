package com.mgnegypt.previewmd.data.update

/**
 * A semantic version (semver.org 2.0.0) with the full precedence rules,
 * including prerelease ordering: a release outranks its prereleases,
 * numeric identifiers compare numerically and sort before alphanumeric
 * ones, and a shorter identifier list sorts before a longer one when
 * they share a prefix. Build metadata is parsed but ignored for
 * precedence, per spec.
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: List<String> = emptyList(),
    val build: String? = null
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)
        if (prerelease.isEmpty() && other.prerelease.isEmpty()) return 0
        if (prerelease.isEmpty()) return 1
        if (other.prerelease.isEmpty()) return -1
        val shared = minOf(prerelease.size, other.prerelease.size)
        for (i in 0 until shared) {
            val a = prerelease[i]
            val b = other.prerelease[i]
            if (a == b) continue
            val aNumeric = a.toLongOrNull()
            val bNumeric = b.toLongOrNull()
            return when {
                aNumeric != null && bNumeric != null ->
                    aNumeric.compareTo(bNumeric)

                aNumeric != null -> -1
                bNumeric != null -> 1
                else -> a.compareTo(b)
            }
        }
        return prerelease.size.compareTo(other.prerelease.size)
    }

    fun isNewerThan(other: SemVer): Boolean = this > other

    override fun toString(): String = buildString {
        append(major).append('.').append(minor).append('.').append(patch)
        if (prerelease.isNotEmpty()) {
            append('-').append(prerelease.joinToString("."))
        }
        if (build != null) {
            append('+').append(build)
        }
    }

    companion object {
        private val PATTERN = Regex(
            """^(\d+)\.(\d+)\.(\d+)""" +
                """(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?""" +
                """(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$"""
        )

        /** Parses "1.2.3", "v1.2.3" and "1.2.3-beta.1"; null if invalid. */
        fun parse(raw: String): SemVer? {
            val cleaned = raw.trim().removePrefix("v")
            val match = PATTERN.matchEntire(cleaned) ?: return null
            val (major, minor, patch, pre, build) = match.destructured
            return SemVer(
                major = major.toInt(),
                minor = minor.toInt(),
                patch = patch.toInt(),
                prerelease = if (pre.isEmpty()) {
                    emptyList()
                } else {
                    pre.split(".")
                },
                build = build.ifEmpty { null }
            )
        }
    }
}

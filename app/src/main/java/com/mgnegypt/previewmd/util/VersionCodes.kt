package com.mgnegypt.previewmd.util

/**
 * The versionCode formula from the plan (Section 4.2). The release workflow
 * computes the same value in shell; this implementation exists so the formula
 * is unit-tested and reusable, and both must always agree.
 *
 * versionCode = MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + PRE
 * PRE is 99 for a stable release and N for a beta (1.1.0-beta.N), so a stable
 * release always sorts after every beta of the same version.
 */
object VersionCodes {
    fun of(major: Int, minor: Int, patch: Int, beta: Int? = null): Int {
        require(major >= 0) { "major must be >= 0" }
        require(minor in 0..99) { "minor must be in 0..99" }
        require(patch in 0..99) { "patch must be in 0..99" }
        val pre = if (beta == null) {
            99
        } else {
            require(beta in 1..98) { "beta must be in 1..98" }
            beta
        }
        return major * 1_000_000 + minor * 10_000 + patch * 100 + pre
    }
}

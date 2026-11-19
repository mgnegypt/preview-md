package com.mgnegypt.previewmd.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetSelectionTest {

    private fun releaseWith(vararg names: String) = GhRelease(
        tagName = "v1.0.1",
        assets = names.map { GhAsset(name = it, downloadUrl = "https://x/$it") }
    )

    @Test
    fun `picks the apk by name pattern, never by position`() {
        val release = releaseWith(
            "preview-md-1.0.1.apk.sha256",
            "notes.txt",
            "preview-md-1.0.1.apk",
            "source.zip"
        )
        val (apk, checksum) = selectAssets(release)!!
        assertEquals("preview-md-1.0.1.apk", apk.name)
        assertEquals("preview-md-1.0.1.apk.sha256", checksum?.name)
    }

    @Test
    fun `no apk asset means no selection`() {
        assertNull(selectAssets(releaseWith("other.apk", "preview-md.apk.txt")))
    }

    @Test
    fun `missing checksum pairs as null`() {
        val (apk, checksum) = selectAssets(
            releaseWith("preview-md-2.0.0-beta.1.apk")
        )!!
        assertEquals("preview-md-2.0.0-beta.1.apk", apk.name)
        assertNull(checksum)
    }
}

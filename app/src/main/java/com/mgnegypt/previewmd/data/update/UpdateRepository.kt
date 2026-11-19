package com.mgnegypt.previewmd.data.update

import android.content.Context
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/** The release channel the user picked in Settings. */
enum class UpdateChannel {
    STABLE,
    BETA
}

@Serializable
data class GhAsset(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("size") val size: Long = 0
)

@Serializable
data class GhRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String? = null,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("body") val body: String? = null,
    @SerialName("assets") val assets: List<GhAsset> = emptyList()
)

/** A release the updater can act on: version, notes and both assets. */
data class ReleaseInfo(
    val version: SemVer,
    val tag: String,
    val notes: String?,
    val apk: GhAsset,
    val checksum: GhAsset
)

enum class UpdateErrorKind {
    OFFLINE,
    RATE_LIMITED,
    NO_RELEASE,
    NO_APK_ASSET,
    NO_CHECKSUM,
    SERVER,
    PARSE,
    DOWNLOAD_FAILED,
    CHECKSUM_MISMATCH,
    INSTALL_FAILED
}

class UpdateException(
    val kind: UpdateErrorKind,
    cause: Throwable? = null
) : Exception(cause?.message, cause)

sealed interface UpdateProgress {
    data class Downloading(val fraction: Float) : UpdateProgress
    data object Verifying : UpdateProgress
    data class Ready(val file: File) : UpdateProgress
}

/**
 * Picks the APK asset strictly by the release contract's name pattern
 * (never by list position) and pairs it with its `<name>.sha256`
 * checksum asset. Pure and unit-tested.
 */
fun selectAssets(release: GhRelease): Pair<GhAsset, GhAsset?>? {
    val apk = release.assets.firstOrNull { APK_NAME_REGEX.matches(it.name) }
        ?: return null
    val checksum = release.assets.firstOrNull {
        it.name == apk.name + ".sha256"
    }
    return apk to checksum
}

val APK_NAME_REGEX = Regex("""^preview-md-(.+)\.apk$""")

/**
 * Talks to the GitHub Releases API for mgnegypt/preview-md. Requests
 * carry the current API version header (2026-03-10, verified from
 * GitHub's API versions documentation) and an ETag when a cached
 * response exists, keeping the app well inside the 60 requests/hour
 * unauthenticated budget.
 */
class UpdateRepository(
    context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {

    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * The release the given channel would update to: for STABLE the
     * latest non-prerelease release, for BETA the newest release
     * including prereleases. Null when the repo has no usable release.
     */
    suspend fun fetchCandidate(channel: UpdateChannel): ReleaseInfo? =
        withContext(Dispatchers.IO) {
            when (channel) {
                UpdateChannel.STABLE -> {
                    val body = get(LATEST_URL, "latest") ?: return@withContext null
                    val release = try {
                        json.decodeFromString<GhRelease>(body)
                    } catch (e: Exception) {
                        throw UpdateException(UpdateErrorKind.PARSE, e)
                    }
                    toReleaseInfo(release, requireChecksum = true)
                }

                UpdateChannel.BETA -> {
                    val body = get(LIST_URL, "list") ?: return@withContext null
                    val releases = try {
                        json.decodeFromString<List<GhRelease>>(body)
                    } catch (e: Exception) {
                        throw UpdateException(UpdateErrorKind.PARSE, e)
                    }
                    releases
                        .filter { !it.draft }
                        .mapNotNull {
                            runCatching {
                                toReleaseInfo(it, requireChecksum = true)
                            }.getOrNull()
                        }
                        .maxByOrNull { it.version }
                }
            }
        }

    private fun toReleaseInfo(
        release: GhRelease,
        requireChecksum: Boolean
    ): ReleaseInfo {
        val version = SemVer.parse(release.tagName)
            ?: throw UpdateException(UpdateErrorKind.PARSE)
        val (apk, checksum) = selectAssets(release)
            ?: throw UpdateException(UpdateErrorKind.NO_APK_ASSET)
        if (requireChecksum && checksum == null) {
            throw UpdateException(UpdateErrorKind.NO_CHECKSUM)
        }
        return ReleaseInfo(
            version = version,
            tag = release.tagName,
            notes = release.body,
            apk = apk,
            checksum = checksum!!
        )
    }

    /**
     * Downloads the checksum, then the APK into the cache directory,
     * emitting progress; verifies the SHA-256 (the checksum file's first
     * whitespace-separated token) and deletes the file on mismatch.
     */
    fun downloadAndVerify(info: ReleaseInfo): Flow<UpdateProgress> = flow {
        val dir = File(appContext.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(dir, info.apk.name)
        try {
            val expected = downloadText(info.checksum.downloadUrl)
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.lowercase()
                ?: throw UpdateException(UpdateErrorKind.NO_CHECKSUM)

            emit(UpdateProgress.Downloading(0f))
            downloadToFile(info.apk.downloadUrl, apkFile) { fraction ->
                emit(UpdateProgress.Downloading(fraction))
            }
            emit(UpdateProgress.Verifying)
            val actual = sha256Hex(apkFile)
            if (!actual.equals(expected, ignoreCase = true)) {
                apkFile.delete()
                throw UpdateException(UpdateErrorKind.CHECKSUM_MISMATCH)
            }
            emit(UpdateProgress.Ready(apkFile))
        } catch (e: UpdateException) {
            throw e
        } catch (e: IOException) {
            apkFile.delete()
            throw UpdateException(UpdateErrorKind.DOWNLOAD_FAILED, e)
        }
    }.flowOn(Dispatchers.IO)

    /** GET with ETag caching; null only for a genuine 404. */
    private fun get(url: String, cacheKey: String): String? {
        val cacheDirFile = File(appContext.cacheDir, "update-http").apply {
            mkdirs()
        }
        val etagFile = File(cacheDirFile, "$cacheKey.etag")
        val bodyFile = File(cacheDirFile, "$cacheKey.json")
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", GITHUB_API_VERSION)
            .apply {
                if (etagFile.exists() && bodyFile.exists()) {
                    header("If-None-Match", etagFile.readText().trim())
                }
            }
            .build()
        try {
            client.newCall(request).execute().use { response ->
                return when {
                    response.code == 304 && bodyFile.exists() ->
                        bodyFile.readText()

                    response.code == 404 -> null

                    response.code == 403 || response.code == 429 ->
                        throw UpdateException(UpdateErrorKind.RATE_LIMITED)

                    !response.isSuccessful ->
                        throw UpdateException(UpdateErrorKind.SERVER)

                    else -> {
                        val body = response.body?.string()
                            ?: throw UpdateException(UpdateErrorKind.PARSE)
                        response.header("ETag")?.let {
                            etagFile.writeText(it)
                        }
                        bodyFile.writeText(body)
                        body
                    }
                }
            }
        } catch (e: UpdateException) {
            throw e
        } catch (e: IOException) {
            throw UpdateException(UpdateErrorKind.OFFLINE, e)
        }
    }

    private fun downloadText(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw UpdateException(UpdateErrorKind.DOWNLOAD_FAILED)
            }
            return response.body?.string()
                ?: throw UpdateException(UpdateErrorKind.DOWNLOAD_FAILED)
        }
    }

    private suspend fun downloadToFile(
        url: String,
        target: File,
        onProgress: suspend (Float) -> Unit
    ) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw UpdateException(UpdateErrorKind.DOWNLOAD_FAILED)
            }
            val body = response.body
                ?: throw UpdateException(UpdateErrorKind.DOWNLOAD_FAILED)
            val total = body.contentLength()
            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        read += count
                        if (total > 0) {
                            onProgress(read.toFloat() / total.toFloat())
                        }
                    }
                }
            }
        }
    }

    private fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val GITHUB_API_VERSION = "2026-03-10"
        private const val LATEST_URL =
            "https://api.github.com/repos/mgnegypt/preview-md/releases/latest"
        private const val LIST_URL =
            "https://api.github.com/repos/mgnegypt/preview-md/releases?per_page=20"
    }
}

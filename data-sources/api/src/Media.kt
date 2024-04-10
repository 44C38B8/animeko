package me.him188.ani.datasources.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation

/**
 * Describes properties of a media (video) for a specific episode.
 *
 * Episodes can have different medias from different sources. For example, there are many subtitle alliances.
 */
@Immutable
sealed interface Media {
    /**
     * Globally unique id which includes the source, like "dmhy.1"
     */
    val mediaId: String
    val mediaSourceId: String // e.g. "dmhy"
    val originalUrl: String

    /**
     * A [ResourceLocation] describing how to download the media.
     *
     * Implementation notes:
     *
     */
    val download: ResourceLocation

    val originalTitle: String

    /**
     * Size of the media file. Can be [FileSize.Zero] if not available.
     */
    val size: FileSize
    val publishedTime: Long
    val properties: MediaProperties

    /**
     * Location of the media.
     * @see MediaSourceLocation
     */
    val location: MediaSourceLocation
}

/**
 * The default implementation of [Media].
 *
 * [location] can be either [MediaSourceLocation.ONLINE] or [MediaSourceLocation.LOCAL].
 * A local [MediaSource] may support caching of other [MediaSourceLocation.ONLINE] media sources, however,
 * it may also produce [MediaSourceLocation.LOCAL] [DefaultMedia]s directly when you first search for that media.
 */
@Immutable
@Serializable
class DefaultMedia(
    override val mediaId: String,
    override val mediaSourceId: String, // e.g. "dmhy"
    override val originalUrl: String,
    override val download: ResourceLocation,
    override val originalTitle: String,
    override val size: FileSize,
    override val publishedTime: Long,
    override val properties: MediaProperties,
    override val location: MediaSourceLocation = MediaSourceLocation.ONLINE
) : Media

/**
 * A media that is already cached onto a easily accessible location, i.e. [MediaSourceLocation.LOCAL].
 */
@Immutable
class CachedMedia(
    private val origin: Media,
    cacheMediaSourceId: String,
    override val download: ResourceLocation,
) : Media by origin {
    override val mediaId: String = "${cacheMediaSourceId}-${origin.mediaId}"
    override val mediaSourceId: String = cacheMediaSourceId
    override val location: MediaSourceLocation get() = MediaSourceLocation.LOCAL
}

/**
 * Properties of [Media] that might be useful for filtering.
 */
@Immutable
@Serializable
class MediaProperties(
    /**
     * Empty list means no subtitles
     */
    val subtitleLanguages: List<String>,
    /**
     * Resolution, e.g. "1080P", "4K"
     */
    val resolution: String,
    /**
     * Subtitle group
     */
    val alliance: String,
)

/**
 * Additional data stored when creating the cache to help matching caches with future [request][MediaFetchRequest]s.
 */ // See `MediaFetchRequest.matches` or `MediaCacheStorage`
@Immutable
@Serializable
class MediaCacheMetadata(
//    /**
//     * Id of the [MediaSource] that cached this media.
//     */
//    val cacheMediaSourceId: String, // e.g. "localfs" for the local file system
    /**
     * @see MediaFetchRequest.episodeId
     */
    val episodeId: String?,
    val subjectNames: List<String>,
    val episodeSort: String,
    val episodeName: String,
    val extra: Map<String, String> = emptyMap(),
)

package com.musicx.player.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class UiTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnail: String
)

interface MusicXApiService {
    @GET
    suspend fun getAudioStreamByFullUrl(@Url fullUrl: String): JsonObject

    @GET
    suspend fun getWatchQueueByFullUrl(@Url fullUrl: String): JsonObject

    @GET("/api/status")
    suspend fun getSystemStatus(): JsonObject

    @GET("/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("filter") filter: String? = "songs"
    ): JsonArray

    @GET("/browse/home")
    suspend fun getHomeFeed(@Query("limit") limit: Int = 3): JsonArray

    @GET("/browse/charts")
    suspend fun getCharts(@Query("country") country: String = "IN"): JsonObject

    @GET("/lyrics/{id}")
    suspend fun getLyrics(@Path("id") id: String): JsonObject
}

object NetworkClient {
    const val DEFAULT_RENDER_URL = "https://music-x-api-docs.onrender.com"
    var streamTunnelUrl: String = "https://music-x-api-docs.onrender.com"
        private set

    fun setStreamTunnel(url: String) {
        streamTunnelUrl = url.trim().trimEnd('/')
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: MusicXApiService = Retrofit.Builder()
        .baseUrl(DEFAULT_RENDER_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MusicXApiService::class.java)

    suspend fun fetchStream(videoId: String): JsonObject {
        return api.getAudioStreamByFullUrl("$streamTunnelUrl/stream/$videoId")
    }

    // Safely parses the nested YTMusic watch response: {"tracks": [...]}
    suspend fun fetchWatchQueueTracks(videoId: String): List<UiTrack> {
        return try {
            val response = api.getWatchQueueByFullUrl("$streamTunnelUrl/watch/$videoId?radio=true")
            val tracksArray = when {
                response.has("tracks") && response.get("tracks").isJsonArray -> response.getAsJsonArray("tracks")
                else -> JsonArray()
            }
            parseTracks(tracksArray)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseTracks(array: JsonArray): List<UiTrack> {
        val list = mutableListOf<UiTrack>()
        for (elem in array) {
            if (!elem.isJsonObject) continue
            val obj = elem.asJsonObject
            val id = obj.get("videoId")?.asString ?: continue
            val title = obj.get("title")?.asString ?: "Unknown Title"

            val artist = when {
                obj.has("artists") && obj.get("artists").isJsonArray -> {
                    val arr = obj.getAsJsonArray("artists")
                    if (arr.size() > 0) arr[0].asJsonObject.get("name")?.asString ?: "Artist" else "Artist"
                }
                obj.has("artist") -> obj.get("artist").asString
                else -> "Artist"
            }

            var thumb = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"
            if (obj.has("thumbnails") && obj.get("thumbnails").isJsonArray) {
                val thumbs = obj.getAsJsonArray("thumbnails")
                if (thumbs.size() > 0) {
                    thumb = thumbs[thumbs.size() - 1].asJsonObject.get("url")?.asString ?: thumb
                }
            } else if (obj.has("thumbnail")) {
                thumb = obj.get("thumbnail").asString
            }

            list.add(UiTrack(id, title, artist, thumb))
        }
        return list
    }
}

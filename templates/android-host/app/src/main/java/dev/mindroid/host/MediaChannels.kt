package dev.mindroid.host

import android.content.ContentUris
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class MediaChannels(private val context: Context) : BridgeChannel {
    override val name: String = "media"

    private var player: MediaPlayer? = null

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "play" -> {
                val uri = args?.get("uri") as? String ?: return mapOf("error" to "invalid-uri")
                play(uri)
            }

            "pause" -> {
                player?.pause()
                mapOf("ok" to true)
            }

            "stop" -> {
                player?.stop()
                player?.release()
                player = null
                mapOf("ok" to true)
            }

            "resume" -> {
                player?.start()
                mapOf("ok" to true)
            }

            "seek" -> {
                val ms = (args?.get("ms") as? Number)?.toInt() ?: return mapOf("error" to "invalid-ms")
                player?.seekTo(ms)
                mapOf("ok" to true)
            }

            "info" -> {
                val p = player ?: return mapOf("error" to "no-active-playback")
                mapOf(
                    "duration" to p.duration,
                    "position" to p.currentPosition,
                    "playing" to p.isPlaying
                )
            }

            "listAudio" -> listMedia(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
            )

            "listVideo" -> listMedia(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DURATION
                )
            )

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun play(uri: String): Any {
        player?.release()
        player = null
        return try {
            val p = MediaPlayer()
            p.setAudioStreamType(AudioManager.STREAM_MUSIC)
            p.setDataSource(context, Uri.parse(uri))
            p.prepare()
            p.start()
            player = p
            mapOf("ok" to true, "duration" to p.duration)
        } catch (e: Exception) {
            Log.e("MediaChannels", "play failed: ${e.message}")
            mapOf("error" to (e.message ?: "playback-error"))
        }
    }

    private fun listMedia(collectionUri: Uri, projection: Array<String>): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        context.contentResolver.query(collectionUri, projection, null, null, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndex(projection[0])
            val titleIdx = cursor.getColumnIndex(projection[1])
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val contentUri = ContentUris.withAppendedId(collectionUri, id)
                val entry = mutableMapOf<String, Any?>(
                    "id" to id,
                    "uri" to contentUri.toString(),
                    "title" to cursor.getString(titleIdx)
                )
                if (projection.size > 2) {
                    entry["extra"] = cursor.getString(2)
                }
                results.add(entry)
            }
        }
        return results
    }
}

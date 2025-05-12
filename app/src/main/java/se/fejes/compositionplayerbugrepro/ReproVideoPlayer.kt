package se.fejes.compositionplayerbugrepro

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.CompositionPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.ui.PlayerView

private const val TAG = "ReproVideoPlayer"

data class Clip(val uri: String, val duration: Double)

@Composable
fun ReproVideoPlayer(modifier: Modifier = Modifier, clips: List<Clip>) {
    val playerManager = remember { CompositionVideoPlayerManager() }

    AndroidView(
        modifier = modifier,
        factory = { context -> playerManager.createView(context, clips) }
    )

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }
}

class CompositionVideoPlayerManager() {
    private var player: Player? = null

    @OptIn(UnstableApi::class)
    fun createView(context: Context, clips: List<Clip>): View {
        release()
        val player = createCompositionPlayer(context, clips)

        this.player = player
        return PlayerView(context).apply {
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            controllerShowTimeoutMs = 0
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowRewindButton(false)
            setShowFastForwardButton(false)
            controllerAutoShow = true

            setPlayer(player)
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}

@SuppressLint("RestrictedApi")
@OptIn(UnstableApi::class)
private fun createCompositionPlayer(context: Context, clips: List<Clip>): CompositionPlayer {
    val mediaItems = clips.mapNotNull { clip -> createEditedMediaItem(context, clip) }
    val videoSequence = EditedMediaItemSequence.Builder(mediaItems).build()
    val composition = Composition.Builder(videoSequence).build()
    return CompositionPlayer.Builder(context).build().apply {
        setComposition(composition)
        prepare()
    }
}

@OptIn(UnstableApi::class)
private fun createEditedMediaItem(context: Context, clip: Clip): EditedMediaItem? {
    try {
        context.assets.openFd(clip.uri.removePrefix("asset:///")).close()
        Log.d(TAG, "Media file found in assets: ${clip.uri}")
    } catch (e: Exception) {
        throw RuntimeException("Media file not found in assets: ${clip.uri}", e)
    }

    val endTimeUs = (clip.duration * 1000000).toLong()
    val isPhoto = !clip.uri.endsWith(".mp4")

    val clippingConfiguration = MediaItem.ClippingConfiguration.Builder()
        .setEndPositionUs(endTimeUs)
        .build()

    val mediaItem = MediaItem.Builder().apply {
        setUri(clip.uri)
        setClippingConfiguration(clippingConfiguration)
        if (isPhoto) {
            val durationUs = clippingConfiguration.endPositionUs - clippingConfiguration.startPositionUs
            setImageDurationMs(durationUs / 1000)
        }
    }.build()

    return EditedMediaItem.Builder(mediaItem).apply {
        setDurationUs(endTimeUs)
        if (isPhoto)
            setFrameRate(30)
    }.build()
}

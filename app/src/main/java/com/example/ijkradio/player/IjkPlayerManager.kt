package com.example.ijkradio.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.example.ijkradio.data.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IjkPlayerManager private constructor(context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentStation: Station? = null
    private val appContext = context.applicationContext

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Stopped)
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        initPlayer()
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)

            setOnPreparedListener {
                start()
                _state.value = PlaybackState.Playing(currentStation?.name ?: "")
            }
            setOnErrorListener { _, what, extra ->
                _state.value = PlaybackState.Error("播放错误 [$what,$extra]")
                false
            }
            setOnCompletionListener {
                _state.value = PlaybackState.Stopped
            }
            setOnBufferingUpdateListener { _, percent ->
                if (percent < 100) {
                    if (_state.value !is PlaybackState.Buffering) {
                        _state.value = PlaybackState.Buffering
                    }
                } else {
                    if (isPlaying) {
                        _state.value = PlaybackState.Playing(currentStation?.name ?: "")
                    }
                }
            }
        }
    }

    fun playStation(station: Station) {
        currentStation = station
        mediaPlayer?.apply {
            reset()
            setDataSource(appContext, Uri.parse(station.url))
            prepareAsync()
        }
        _state.value = PlaybackState.Buffering
    }

    fun pause() {
        mediaPlayer?.pause()
        _state.value = PlaybackState.Paused
    }

    fun resume() {
        mediaPlayer?.start()
        _state.value = PlaybackState.Playing(currentStation?.name ?: "")
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        currentStation = null
        _state.value = PlaybackState.Stopped
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        @Volatile
        private var INSTANCE: IjkPlayerManager? = null

        fun getInstance(context: Context): IjkPlayerManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: IjkPlayerManager(context.applicationContext).also { INSTANCE = it }
        }
    }
}
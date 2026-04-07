package com.example.ijkradio.player

import android.content.Context
import tv.danmaku.ijk.media.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import com.example.ijkradio.data.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IjkPlayerManager private constructor(context: Context) {

    private var ijkPlayer: IjkMediaPlayer? = null
    private var currentStation: Station? = null

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Stopped)
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.nativeProfileBegin("libijkplayer.so")
        initPlayer()
    }

    private fun initPlayer() {
        ijkPlayer = IjkMediaPlayer().apply {
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024)

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
            setOnInfoListener { _, what, _ ->
                when (what) {
                    IMediaPlayer.MEDIA_INFO_BUFFERING_START -> _state.value = PlaybackState.Buffering
                    IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        if (isPlaying()) {
                            _state.value = PlaybackState.Playing(currentStation?.name ?: "")
                        }
                    }
                }
                true
            }
        }
    }

    fun playStation(station: Station) {
        currentStation = station
        ijkPlayer?.apply {
            stop()
            reset()
            dataSource = station.url
            prepareAsync()
        }
        _state.value = PlaybackState.Buffering
    }

    fun pause() {
        ijkPlayer?.pause()
        _state.value = PlaybackState.Paused
    }

    fun resume() {
        ijkPlayer?.start()
        _state.value = PlaybackState.Playing(currentStation?.name ?: "")
    }

    fun stop() {
        ijkPlayer?.stop()
        ijkPlayer?.reset()
        currentStation = null
        _state.value = PlaybackState.Stopped
    }

    fun isPlaying(): Boolean = ijkPlayer?.isPlaying() ?: false

    fun setVolume(volume: Float) {
        ijkPlayer?.setVolume(volume, volume)
    }

    fun release() {
        stop()
        ijkPlayer?.release()
        ijkPlayer = null
        IjkMediaPlayer.nativeProfileEnd()
    }

    companion object {
        @Volatile
        private var INSTANCE: IjkPlayerManager? = null

        fun getInstance(context: Context): IjkPlayerManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: IjkPlayerManager(context.applicationContext).also { INSTANCE = it }
        }
    }
}

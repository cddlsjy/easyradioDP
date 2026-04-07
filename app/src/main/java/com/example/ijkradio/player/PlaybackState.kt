package com.example.ijkradio.player

sealed class PlaybackState {
    object Stopped : PlaybackState()
    object Buffering : PlaybackState()
    data class Playing(val stationName: String) : PlaybackState()
    object Paused : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

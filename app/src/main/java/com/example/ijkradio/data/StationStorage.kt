package com.example.ijkradio.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StationStorage(private val context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("radio_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val STATIONS_KEY = "stations"
        private const val LAST_PLAYED_KEY = "last_played"
    }

    fun loadStations(): MutableList<Station> {
        val json = prefs.getString(STATIONS_KEY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<Station>>() {}.type)
        } else {
            defaultStations()
        }
    }

    fun saveStations(stations: List<Station>) {
        val json = gson.toJson(stations)
        prefs.edit().putString(STATIONS_KEY, json).apply()
    }

    fun saveLastPlayed(station: Station) {
        val json = gson.toJson(station)
        prefs.edit().putString(LAST_PLAYED_KEY, json).apply()
    }

    fun getLastPlayed(): Station? {
        val json = prefs.getString(LAST_PLAYED_KEY, null)
        return if (json != null) {
            gson.fromJson(json, Station::class.java)
        } else {
            null
        }
    }

    private fun defaultStations(): MutableList<Station> {
        return mutableListOf(
            Station("央广新闻", "http://ngcdn001.cnr.cn/live/zgzs/index.m3u8"),
            Station("中央音乐台", "http://ngcdn004.cnr.cn/live/zbge/index.m3u8"),
            Station("经典音乐台", "http://ngcdn003.cnr.cn/live/dszs/index.m3u8"),
            Station("中国交通广播", "http://ngcdn002.cnr.cn/live/jtfs/index.m3u8"),
            Station("北京音乐广播", "https://ls.live.bjradio.cn:8080/youyi"),
            Station("国际广播电台", "http://ngcdn001.cnr.cn/live/gjgb/index.m3u8")
        )
    }
}

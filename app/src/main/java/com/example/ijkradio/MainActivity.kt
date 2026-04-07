package com.example.ijkradio

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ijkradio.data.Station
import com.example.ijkradio.data.StationStorage
import com.example.ijkradio.databinding.ActivityMainBinding
import com.example.ijkradio.databinding.DialogAddStationBinding
import com.example.ijkradio.player.IjkPlayerManager
import com.example.ijkradio.player.PlaybackState
import com.example.ijkradio.ui.StationAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: StationStorage
    private lateinit var playerManager: IjkPlayerManager
    private lateinit var adapter: StationAdapter

    private var currentVolume = 0.8f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StationStorage(this)
        playerManager = IjkPlayerManager.getInstance(this)

        setupRecyclerView()
        setupButtons()
        setupVolumeControl()
        observePlayerState()
        restoreLastPlayed()
    }

    private fun setupRecyclerView() {
        adapter = StationAdapter(storage.loadStations()) { station ->
            playerManager.playStation(station)
            storage.saveLastPlayed(station)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.buttonAdd.setOnClickListener {
            showAddStationDialog()
        }

        binding.buttonDelete.setOnClickListener {
            val position = adapter.getSelectedPosition()
            if (position == -1) {
                Toast.makeText(this, "请先选择要删除的电台", Toast.LENGTH_SHORT).show()
            } else {
                showDeleteConfirmation(position)
            }
        }

        binding.buttonPlayPause.setOnClickListener {
            val selectedPosition = adapter.getSelectedPosition()
            val station = adapter.getStationAt(selectedPosition)

            when (playerManager.state.value) {
                is PlaybackState.Playing, is PlaybackState.Buffering -> {
                    playerManager.stop()
                }
                is PlaybackState.Paused -> {
                    playerManager.resume()
                }
                else -> {
                    if (station != null) {
                        playerManager.playStation(station)
                        storage.saveLastPlayed(station)
                    } else {
                        Toast.makeText(this, "请先选择要播放的电台", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupVolumeControl() {
        binding.seekBarVolume.progress = (currentVolume * 100).toInt()
        binding.textVolumeValue.text = "${(currentVolume * 100).toInt()}%"

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress / 100f
                binding.textVolumeValue.text = "$progress%"
                playerManager.setVolume(currentVolume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun observePlayerState() {
        lifecycleScope.launch {
            playerManager.state.collect { state ->
                when (state) {
                    is PlaybackState.Playing -> {
                        binding.buttonPlayPause.text = getString(R.string.stop)
                        binding.textStatus.text = "正在播放: ${state.stationName}"
                    }
                    is PlaybackState.Buffering -> {
                        binding.textStatus.text = "缓冲中..."
                    }
                    is PlaybackState.Paused -> {
                        binding.buttonPlayPause.text = getString(R.string.play)
                        binding.textStatus.text = "已暂停"
                    }
                    is PlaybackState.Stopped -> {
                        binding.buttonPlayPause.text = getString(R.string.play)
                        binding.textStatus.text = "已停止"
                    }
                    is PlaybackState.Error -> {
                        binding.textStatus.text = "错误: ${state.message}"
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun restoreLastPlayed() {
        val lastPlayed = storage.getLastPlayed()
        if (lastPlayed != null) {
            val stations = storage.loadStations()
            val index = stations.indexOfFirst { it.name == lastPlayed.name && it.url == lastPlayed.url }
            if (index != -1) {
                adapter.setSelectedPosition(index)
            }
        }
    }

    private fun showAddStationDialog() {
        val dialogBinding = DialogAddStationBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("添加电台")
            .setView(dialogBinding.root)
            .setPositiveButton("确定") { _, _ ->
                val name = dialogBinding.editStationName.text.toString().trim()
                val url = dialogBinding.editStationUrl.text.toString().trim()

                if (name.isNotEmpty() && url.isNotEmpty()) {
                    val stations = storage.loadStations()
                    stations.add(Station(name, url))
                    storage.saveStations(stations)
                    adapter.updateStations(stations)
                    Toast.makeText(this, "电台已添加", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirmation(position: Int) {
        val station = adapter.getStationAt(position) ?: return

        AlertDialog.Builder(this)
            .setTitle("删除电台")
            .setMessage("确定要删除 \"${station.name}\" 吗？")
            .setPositiveButton("确定") { _, _ ->
                val currentState = playerManager.state.value
                val isPlayingThisStation = (currentState is PlaybackState.Playing &&
                        currentState.stationName == station.name)

                if (isPlayingThisStation) {
                    playerManager.stop()
                }

                val stations = storage.loadStations()
                stations.removeAt(position)
                storage.saveStations(stations)
                adapter.removeAt(position)
                Toast.makeText(this, "电台已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}

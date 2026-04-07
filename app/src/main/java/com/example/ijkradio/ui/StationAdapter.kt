package com.example.ijkradio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ijkradio.data.Station
import com.example.ijkradio.databinding.ItemStationBinding

class StationAdapter(
    private var stations: MutableList<Station>,
    private val onItemClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    private var selectedPosition: Int = -1

    inner class StationViewHolder(private val binding: ItemStationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
                    onItemClick(stations[position])
                }
            }
        }

        fun bind(station: Station, isSelected: Boolean) {
            binding.textStationName.text = station.name
            binding.textStationUrl.text = station.url
            binding.root.isSelected = isSelected
            binding.root.setBackgroundColor(
                if (isSelected) 0xFFE0E0E0.toInt() else 0xFFF5F5F5.toInt()
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemStationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(stations[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = stations.size

    fun getSelectedPosition(): Int = selectedPosition

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected)
        }
        notifyItemChanged(selectedPosition)
    }

    fun removeAt(position: Int): Station {
        val station = stations.removeAt(position)
        if (selectedPosition == position) {
            selectedPosition = -1
        } else if (selectedPosition > position) {
            selectedPosition--
        }
        notifyItemRemoved(position)
        if (position < stations.size) {
            notifyItemChanged(position)
        }
        return station
    }

    fun getStationAt(position: Int): Station? {
        return if (position in stations.indices) stations[position] else null
    }

    fun updateStations(newStations: List<Station>) {
        stations.clear()
        stations.addAll(newStations)
        selectedPosition = -1
        notifyDataSetChanged()
    }
}

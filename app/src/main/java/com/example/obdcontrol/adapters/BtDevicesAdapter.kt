package com.example.obdcontrol.adapters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import java.util.*

class BtDevicesAdapter : RecyclerView.Adapter<BtDevicesAdapter.ViewHolder>() {

    val list = mutableListOf<BluetoothDevice>()
    var selected = -1
    var matchOnlySppDevice = false

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "BtDeviceAdapter::onAttachedToRecyclerView")
        refresh()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        Log.v(Const.TAG, "BleDeviceAdapter::onCreateViewHolder")
        val contentView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(contentView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Log.v(Const.TAG, "BtDeviceAdapter::onBindViewHolder")
        holder.name.text = list.get(position).name
        holder.select.setOnClickListener{
            if (position != selected) {
                Log.v(Const.TAG, "BtDeviceAdapter::onClickListener position = $position")
                selected = position
            } else {
                // reset color
                selected = -1
            }
        }
        return
    }

    fun refresh() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter?.run {
            list.clear()
            val newList = adapter.bondedDevices
                .filter {
                    !matchOnlySppDevice and
                    it.uuids.contains(ParcelUuid(UUID.fromString(Const.UUIDS.SPP_UUID)))
                }
            list.addAll(newList)
        }
    }

    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.text_item_device_name)
        val select = view.findViewById<ImageButton>(R.id.button_item_select)
    }
}
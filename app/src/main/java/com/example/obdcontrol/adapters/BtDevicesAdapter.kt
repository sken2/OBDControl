package com.example.obdcontrol.adapters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.ui.DeviceSelectFragment
import java.lang.Exception
import java.util.*

class BtDevicesAdapter : RecyclerView.Adapter<BtDevicesAdapter.ViewHolder>() {

    val list = mutableListOf<BluetoothDevice>()
    lateinit var recycler: RecyclerView
    private val deviceSelectFragment by lazy {
        FragmentManager.findFragment<DeviceSelectFragment>(recycler)
    }
//    var selected = -1
    var matchOnlySppDevice = false
    val keyProvider by lazy {
        DevcieKeyProvider(95)
    }
    val selectionTracker by lazy {
        SelectionTracker.Builder<String>(
            "",
            recycler,
            keyProvider,
            DeviceItemLookup,
            StorageStrategy.createStringStorage()
        ).build()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "BtDeviceAdapter::onAttachedToRecyclerView")
        recycler = recyclerView
        selectionTracker.addObserver(deviceSelectFragment.selectionObserver)
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

    fun getItemByKey(key : String) : BluetoothDevice{
        return list.get(keyProvider.getPosition(key))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Log.v(Const.TAG, "BtDeviceAdapter::onBindViewHolder") // so many messages from this
        holder.name.text = list.get(position).name
        holder.isActive(selectionTracker.isSelected(list.get(position).address))
//        holder.select.setOnClickListener{
//            if (position != selected) {
//                Log.v(Const.TAG, "BtDeviceAdapter::onClickListener position = $position")
//                selected = position
//            } else {
//                // reset color
//                selected = -1
//            }
//        }
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

    private val DeviceItemLookup = object : ItemDetailsLookup<String>() {

        override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
            val viewAtEvent = recycler.findChildViewUnder(e.x, e.y)
            viewAtEvent?.let {
                val viewHoler = recycler.getChildViewHolder(it)
                if (viewHoler is ViewHolder) {
                    return viewHoler.getItemDetails()
                }
            }
            return null // really coming here is normial?
        }
    }

    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.text_item_device_name)
        val select = view.findViewById<ImageButton>(R.id.button_item_select)

        fun getItemDetails() : ItemDetail {
            return ItemDetail(absoluteAdapterPosition, list.get(absoluteAdapterPosition).address)
        }

        fun isActive(isActive : Boolean) {
            select.isActivated = isActive
        }
    }

    inner class DevcieKeyProvider(scope : Int) : ItemKeyProvider<String>(scope) {

        override fun getKey(position: Int): String? {
            return list.get(position).address
        }

        override fun getPosition(key: String): Int {
            list.forEachIndexed { index, bluetoothDevice ->
                if (bluetoothDevice.address == key) return index
            }
            throw Exception("oops")
        }
    }

    data class ItemDetail(private val adapterposition : Int, private val selectionKey : String)
        : ItemDetailsLookup.ItemDetails<String>() {
        override fun getSelectionKey(): String? {
            return selectionKey
        }

        override fun getPosition(): Int {
            return adapterposition
        }
    }
}
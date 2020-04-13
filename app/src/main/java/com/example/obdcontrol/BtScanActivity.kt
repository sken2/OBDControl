package com.example.obdcontrol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_bt_scan.*

class BtScanActivity : AppCompatActivity() {

    val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val inflater : LayoutInflater by lazy{
        this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceView = ListView(this)
        deviceView.adapter = deviceAdapter
        deviceView.onItemClickListener = itemClickListener
//        setContentView(R.layout.activity_bt_scan)
        setContentView(deviceView)
//        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
   }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        scan()
    }

    private val deviceAdapter = object : BaseAdapter() {
        var devices :Array<BluetoothDevice> = emptyArray()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (convertView == null) {
                val newView: ViewGroup = inflater.inflate(R.layout.btscan_item, parent, false) as ViewGroup
                newView.findViewById<TextView>(R.id.device_name).text = devices[position].name
                return newView
            }
            return convertView
        }

        override fun getItem(position: Int): Any {
            return devices[position]
        }

        override fun getItemId(position: Int): Long {
            return 1
        }

        override fun getCount(): Int {
            return devices.size
        }

        fun add(device: BluetoothDevice) {
            devices += device
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device:BluetoothDevice? = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        val uuids = device.uuids
//                        for (uuid in uuids) {
//
//                        }
//                        deviceAdapter.add(device)
                        deviceAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private val itemClickListener =
        OnItemClickListener { parent, view, position, id ->
            println((parent.adapter.getItem(position) as BluetoothDevice).address)
        }

    fun scan() {
        if (!btAdapter!!.isDiscovering) {
            btAdapter!!.startDiscovery()
        }
    }
}
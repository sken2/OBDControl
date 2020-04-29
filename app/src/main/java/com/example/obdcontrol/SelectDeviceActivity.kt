package com.example.obdcontrol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.util.*

class SelectDeviceActivity : AppCompatActivity() {

    val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val inflater : LayoutInflater by lazy{
        this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_select_device)
        val listView : ListView? = findViewById(R.id.list_device_to_connect)
        listView?.adapter = deviceAdapter
        listView?.onItemClickListener = itemClickListener
    }

    override fun onResume() {
        super.onResume()
        var devices = btAdapter?.bondedDevices
        if (devices != null) {
            eachDevice@ for (device in devices) {
                for (listedDevice in deviceAdapter.devices) {
                    if (device.equals(listedDevice)) {
                        continue@eachDevice
                    }
                }
                val uuids = device.uuids
                if (uuids.contains(ParcelUuid(UUID.fromString(Const.UUIDS.SPP_UUID)))) {
                    deviceAdapter.devices += device
                }
            }
            deviceAdapter.notifyDataSetChanged()
        }
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
    }

    private val itemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            synchronized(this) {
                val device = parent.adapter.getItem(position) as BluetoothDevice
                Toast.makeText(this,"Connecting", Toast.LENGTH_SHORT).show()
                val connect = Intent(this@SelectDeviceActivity, ConnectingDeviceActivity::class.java)
                    .putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                startActivity(connect)
            }
        }

    private val connectingDialog : AlertDialog by lazy {
        val builder = AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog)
        builder.apply  {
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{dialog, id ->
                Toast.makeText(this@SelectDeviceActivity,"canceled", Toast.LENGTH_SHORT).show()
            })
        }
        builder.create()
    }
}

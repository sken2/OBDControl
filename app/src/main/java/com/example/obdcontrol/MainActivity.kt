package com.example.obdcontrol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    val preference by lazy {
         this.applicationContext.getSharedPreferences(Const.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    val adapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    val controller : android.media.session.MediaController by lazy {
        MediaSession(this.applicationContext, "OOoOo").controller
    }
    private val previousDevice : Button by lazy {
        findViewById(R.id.btn_previous) as Button
    }
    private val selectDevice : Button by lazy {
        findViewById(R.id.btn_devicelist) as Button
    }
    private val REQUEST_SELECT = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        println(controller.packageName)
        registerReceiver(spotifyReceiver, IntentFilter("com.spotify.music.active"))
        selectDevice.setOnClickListener {
            val select = Intent(this.applicationContext, SelectDeviceActivity::class.java)
            startActivity(select)
            selectDevice.isEnabled = false
        }
        previousDevice.setOnClickListener {
            synchronized (this) {
                val chat = Intent(this.applicationContext, ConnectingDeviceActivity::class.java)
                adapter?.run {
                    val address = preference.getString(Const.Preference.PREF_DEVICE, "")
                    val device = this.getRemoteDevice(address)
                    chat.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                    startActivityForResult(chat, REQUEST_SELECT)
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(spotifyReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        selectDevice.isEnabled = true
        val address = preference.getString(Const.Preference.PREF_DEVICE, "")
        when (address) {
            "" -> {
                previousDevice.text = "No Device"
            }
            else -> {
                val device = adapter?.getRemoteDevice(address)
                previousDevice.text = device?.name
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        super.onPause()
    }

    private val spotifyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("Spotify is active")
//            TODO("Not yet implemented")
        }

    }
}

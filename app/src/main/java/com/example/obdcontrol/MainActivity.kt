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
        MediaSession(this.applicationContext, "OOoOo").controller.also {
            println(it.packageName)
        }
    }
    private val previousDevice : Button by lazy {
        findViewById(R.id.btn_previous) as Button
    }
    private val selectDevice : Button by lazy {
        findViewById(R.id.btn_devicelist) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
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
                    startActivityForResult(chat, Const.Requests.REQUEST_SELECT)
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
        previousDevice.text = when (address) {
            "" -> {
                "No Device"
            }
            else -> {
                adapter?.getRemoteDevice(address)?.name
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
        }
    }
}

package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast

class ConnectingDeviceActivity : AppCompatActivity() {

    private val device : BluetoothDevice? by lazy {
        val intent = getIntent()
        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
    }
    private val deviceName : TextView? by lazy {
        findViewById(R.id.text_device_name) as TextView
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (device == null) {
            finishActivity(255)
        }
        setContentView(R.layout.activity_connecting_device)
    }

    override fun onResume() {
        super.onResume()
        Thread() {
            device?.run {
                deviceName?.text = this.name
                Elm327.init(this, this@ConnectingDeviceActivity)
                if (Elm327.isConnected()) {
                    val chat = getIntent()
                        .setClass(this@ConnectingDeviceActivity.applicationContext, SppChatActivity::class.java)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(chat)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        Toast.makeText(this@ConnectingDeviceActivity, "No Device", Toast.LENGTH_SHORT).show()
                    }
                }
                done()
            }
        }.start()
    }

    private fun done() {
        finish()
    }
}

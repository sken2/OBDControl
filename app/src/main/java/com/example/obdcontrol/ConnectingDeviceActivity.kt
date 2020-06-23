package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class ConnectingDeviceActivity : AppCompatActivity() {

    private val device : BluetoothDevice? by lazy {
        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
    }
    private val deviceName : TextView? by lazy {
        findViewById(R.id.text_device_name) as TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "ConnectingDeviceActivity::onCreate")
        super.onCreate(savedInstanceState)
        if (device == null) {
            finishActivity(255)
        }
        setContentView(R.layout.activity_connecting_device)
    }

    override fun onResume() {
        Log.v(Const.TAG, "ConnectingDeviceActivity::onResume")
        super.onResume()
        device?.run {
            deviceName?.text = this.name
            Thread() {
                Elm327.init(this, this@ConnectingDeviceActivity)
                if (Elm327.isConnected()) {
                    with(intent) {
                        setClass(
                            this@ConnectingDeviceActivity.applicationContext,
                            SppChatActivity::class.java
                        )
                        startActivity(this)
                    }
                } else {
                    with(Handler(Looper.getMainLooper())) {
                        post {
                            Toast.makeText(
                                this@ConnectingDeviceActivity,
                                "No Device",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                done()
            }.start()
        }
    }

    private fun done() {
        Log.v(Const.TAG, "ConnactingDeviceActivity::done")
        finish()
    }
}

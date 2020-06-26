package com.example.obdcontrol.ui

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.setup.NotificationSetup
import com.example.obdcontrol.task.ElmCommTask
import java.util.*

class StartupActivity : AppCompatActivity(), ElmCommTask.ConnectionStateListener {

    private val preference by lazy {
        getSharedPreferences(Const.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    val adapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    val deviceName by lazy {
        findViewById<TextView>(R.id.text_device_name)
    }
    var device : BluetoothDevice? = null
    var service : ElmCommTask? = null
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val afterBindQueue = LinkedList<()->Unit>()

    private var autostartChat = true
    private var autostartControl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "StartupActivity::onCreate")
        super.onCreate(savedInstanceState)

        if (adapter == null) {
            Toast.makeText(this, "No Adapter", Toast.LENGTH_LONG).show()
            finish()
        }
        NotificationSetup.makeChannel(this)
        setContentView(R.layout.activity_startup)
        connect()
    }

    override fun onResume() {
        Log.v(Const.TAG, "StartupActivity::onResume")
        super.onResume()
    }

    override fun onDestroy() {
        service?.run {
            applicationContext.unbindService(connection)
            super.onDestroy()
        }
    }

    override fun onConnectionOpened() {
        runOnUiThread{
            deviceName.text = "Connect"
        }
    }

    override fun onConectionInitialized() {
        // TODO
        when {
            autostartChat -> {goChat()}
            autostartControl -> {}//TODO
            else -> {
                // no idea
            }
        }
    }

    override fun onConnectionClosed() {
        runOnUiThread {
            deviceName.text = "Disconnect"
        }
    }

    protected fun connect() {
        val address = preference.getString(Const.Preference.PREF_DEVICE, "")
        if (address!!.isNotEmpty()) {
            device = adapter?.getRemoteDevice(address)?.also {
                Intent(this.applicationContext, ElmCommTask::class.java).apply {
                    putExtra(BluetoothDevice.EXTRA_NAME, it)
                }.run {
                    applicationContext.bindService(this, connection, Context.BIND_AUTO_CREATE)
                    // TODO check preference to transition
                    afterBindQueue.add {
                        service?.setConnectionStateListener(this@StartupActivity)
                    }
                }
                deviceName.text = it.name
            }
        } else {
            deviceName.text = "[No device selected]"
        }
    }

    private fun goChat() {
        findNavController(R.id.nav_main_screen).navigate(R.id.action_splashFragment_to_sppChatFragment)
    }

    val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v(Const.TAG, "MainActivity::onServiceDisconnected()")
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v(Const.TAG, "MainActivity::onServiceConnected()")
            val binder = service as ElmCommTask.LocalBinder
            this@StartupActivity.service = binder.getService()
            afterBindQueue.first.invoke()
        }
    }

    class ConfirmAndGoDialog(val title : String, val callback : Runnable) : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
            return AlertDialog.Builder(activity)
                .setTitle(title)
                .setNegativeButton("Cancel", { dialog, which ->})
                .setPositiveButton("Ok") { dialog, which ->
                    callback.run()
                }
                .create()
        }
    }
}
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
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.entities.BtDeviceViewModel
import com.example.obdcontrol.setup.NotificationSetup
import com.example.obdcontrol.task.ElmCommTask
import java.util.*

class StartupActivity : AppCompatActivity(), ElmCommTask.ConnectionStateListener {

    val preference by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    var service : ElmCommTask? = null
    private val adapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private val deviceInformation by lazy {
        findViewById<TextView>(R.id.text_device_name)
    }
//    private var device : BluetoothDevice? = null
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val afterBindQueue = LinkedList<()->Unit>()
    private val btDeviceViewModel by lazy {
        ViewModelProvider(this).get(BtDeviceViewModel::class.java).also { it ->
            it.information.observe(this, androidx.lifecycle.Observer { builder ->
                deviceInformation.text = builder
            })
        }
    }

    private lateinit var connectSwitch : SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "StartupActivity::onCreate")
        super.onCreate(savedInstanceState)

        if (adapter == null) {
            Toast.makeText(this, "No Adapter", Toast.LENGTH_LONG).show()
            finish()
        }
        NotificationSetup.makeChannel(this)
        setContentView(R.layout.activity_startup)
        preference.getString(Const.Preference.KEY_DEVICE, null)?.let {
            btDeviceViewModel.setDevice(adapter?.getRemoteDevice(it))
        }
        if (preference.getBoolean(getString(R.string.checkbox_autoconnect), false) ) {
            connect()
        }
    }

    override fun onResume() {
        Log.v(Const.TAG, "StartupActivity::onResume")
        super.onResume()
    }

    override fun onDestroy() {
        service?.run {
            applicationContext.unbindService(connection)
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.app_bar_switch)?.actionView?.run {
            findViewById<SwitchCompat>(R.id.switch_connection)?.apply {
                connectSwitch = this
                setOnCheckedChangeListener {view, isCheched ->
                    Log.v(Const.TAG, "StartupActivity::onChanged")
                    if (this.isChecked) {
                        btDeviceViewModel.changeState(BtDeviceViewModel.State.Connecting)
                        connect()
                    } else {
                        disconnect()
                    }
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        with(findNavController(R.id.fragment_main_screen)) {
            when (item.itemId) {
                R.id.menu_logging -> navigate(R.id.action_global_loggingFragment)
                R.id.menu_select -> navigate(R.id.action_global_deviceSearchFragment)
                R.id.menu_settings -> navigate(R.id.action_global_optionFragment)
                R.id.menu_setup_elm327 -> navigate(R.id.action_global_sppChatFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    // implementation of ElmCommTask.ConnectionStateListener
    override fun onConnectionOpened() {
        Log.v(Const.TAG, "StartupActivity::onConnectionOpened")
        runOnUiThread{
            btDeviceViewModel.changeState(BtDeviceViewModel.State.Connected)
            if (!connectSwitch.isChecked) {
                connectSwitch.isChecked = true
            }
        }
    }

    override fun onConectionInitialized() {
        Log.v(Const.TAG, "StartupActivity::onConnectionInitialized")
        if (preference.getBoolean(getString(R.string.checkbox_autoconnect), false)) {
            findNavController(R.id.fragment_main_screen).navigate(R.id.action_global_sppChatFragment)
        }
    }

    override fun onConnectionClosed() {
        Log.v(Const.TAG, "StartupActivity::onConnectionClosed")
        runOnUiThread {
            btDeviceViewModel.changeState(BtDeviceViewModel.State.Disconnect)
            if (connectSwitch.isChecked) {
                connectSwitch.isChecked = false
            }
        }
    }

    protected fun connect() {
        service?.let {
            if (it.isConnected()) return
        }
        val address = preference.getString(Const.Preference.KEY_DEVICE, null)
        address?.let {
            val device = adapter?.getRemoteDevice(address)?.also {
                Intent(this.applicationContext, ElmCommTask::class.java).apply {
                    putExtra(BluetoothDevice.EXTRA_NAME, it)
                }.run {
                    applicationContext.bindService(this, connection, Context.BIND_AUTO_CREATE)
                    // TODO check preference to transition
                    afterBindQueue.add {
                        service?.setConnectionStateListener(this@StartupActivity)
                    }
                }
            }
            btDeviceViewModel.setDevice(device)
        }
    }

    protected fun disconnect() {
        service?.let {
            applicationContext.unbindService(connection)
            service = null
        }
    }

    val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v(Const.TAG, "StartupActivity::onServiceDisconnected()")
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v(Const.TAG, "StartupActivity::onServiceConnected()")
            val binder = service as ElmCommTask.LocalBinder
            this@StartupActivity.service = binder.getService()
            afterBindQueue.forEach { it.invoke() }
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.v(Const.TAG, "StartupActivity::onBindingDied")
            service = null
            super.onBindingDied(name)
        }
    }
}
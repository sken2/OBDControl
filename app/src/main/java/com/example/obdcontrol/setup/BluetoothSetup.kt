package com.example.obdcontrol.setup

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.obdcontrol.Const
import java.util.*

object BluetoothSetup : Observable() {

    val adapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    val preferPlivileges = arrayOf(
        if (Build.VERSION.SDK_INT >=28) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }
    )
    private var btEnable = isAdapterEnabled()
    private var locationElable = false
    private var prefileageFullfill = false
    private var airplaneMode = false
    private var availability = Status.UNKNOWN

    enum class Status(val state :String) {
        OK("ok"),
        NO_ADAPTER("system has no adapter"),
        ADAPTER_IS_OFF("bluetooth is currently off"),
        NO_BLE_FUTURE("system has no BLE future"),
        NEED_PRIVILAGE("some previlage is needed to run"),
        LOCATION_IS_OFF("location is currently off"),
        AIRPLAME_MODE_NOW("air-plane mode is on"),
        UNKNOWN("not initialized yet")
    }

    private fun isAdapterEnabled() : Boolean {
        adapter?.run {
            return isEnabled
        }
        return false
    }

    override fun setChanged() {
        val newState = getState()
        if (newState != availability) {
            super.setChanged()
        }
        availability = newState
    }

    fun onPrevileageChanged(context : Context) {
        var newPrevileageState = true
        for (previleage in preferPlivileges) {
            if (ContextCompat.checkSelfPermission(context, previleage) != PackageManager.PERMISSION_GRANTED) {
                newPrevileageState = false
            }
        }
        if (prefileageFullfill != newPrevileageState) {
            prefileageFullfill = newPrevileageState
            setChanged()
            notifyObservers(availability)
        }
    }

    fun onLocationChanged(context : Context) {
        var newLocationState = true
        (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).run {
            if (Build.VERSION.SDK_INT >= 28) {
                if (!this.isLocationEnabled) {
                    newLocationState = false
                }
                // TODO handle location on Lower than SDK 28
            }
        }
        if (locationElable != newLocationState) {
            locationElable = newLocationState
            setChanged()
            notifyObservers(availability)
        }
    }

    fun getState() :Status {
        return when {
            adapter == null -> Status.NO_ADAPTER
            airplaneMode -> Status.AIRPLAME_MODE_NOW
            !btEnable -> Status.ADAPTER_IS_OFF
            !prefileageFullfill -> Status.NEED_PRIVILAGE
            !locationElable -> Status.LOCATION_IS_OFF
            else -> Status.OK
        }
    }

    val systemStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.v(Const.TAG, "BleService::onReceive")
            intent?.action?.run {
                when (this) {
                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                        when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            BluetoothAdapter.STATE_ON -> {
                                btEnable = true
                                setChanged()
                                notifyObservers(availability)
                            }
                            BluetoothAdapter.STATE_TURNING_OFF -> {
                                btEnable = false
                                setChanged()
                                notifyObservers(availability)
                            }
                        }
                    }
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        airplaneMode = intent.getBooleanExtra("state", false)
                        setChanged()
                        notifyObservers(availability)
                    }
                }
            }
        }
    }

    val intentFilter = object : IntentFilter() {
        init {
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
    }

    interface deviceChooser {
        fun found(device : BluetoothDevice)
    }
    interface deviceSelector {
        fun detected(device : BluetoothDevice)
    }
}
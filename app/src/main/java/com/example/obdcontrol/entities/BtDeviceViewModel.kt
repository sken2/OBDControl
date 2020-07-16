package com.example.obdcontrol.entities

import android.bluetooth.BluetoothDevice
import android.text.SpannableStringBuilder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BtDeviceViewModel : ViewModel() {

    private var state : State = State.NoDevice
    private var device : BluetoothDevice? = null

    val information = MutableLiveData<SpannableStringBuilder> ()

    fun changeState(state : State) {
        this.state = state
        information.value = getBulder()
    }

    fun setDevice(device : BluetoothDevice?) {
        this.device = device?.also {
            state = State.Disconnect
        }

        information.value = getBulder()
    }

    private fun getBulder() : SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        if (device == null) {
            builder.append("[No device is selected]")
        } else {
            builder.append(
                if (device!!.name != null) device!!.name
                else "[${device!!.address}]"
            )
            builder.append(
                when (state) {
                    State.Disconnect -> "Disconnect"
                    State.Connecting -> "Connecting"
                    State.Connected -> "Connected"
                    else -> ""
                }
            )
        }
        return builder
    }

    enum class State {
        NoDevice,
        Disconnect,
        Connecting,
        Connected
    }
}
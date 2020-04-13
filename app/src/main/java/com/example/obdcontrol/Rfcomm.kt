package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.SurfaceControl
import java.io.BufferedInputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Rfcomm : Observable() {
    var device: BluetoothDevice? = null
    var socket: BluetoothSocket? = null
    val executor = Executors.newSingleThreadExecutor()
    var connected : Boolean = false
    private val CR = "\r"
    private val CRLF = "\r\n"

//    private var logging = arrayOf(transaction("", Direction.RX))

    fun init(device: BluetoothDevice) : Boolean {
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            return false
        }
        this.device = device
        return true
    }

    fun connect() {
        if (device == null) {
            return
        }
        val future = executor.submit(Callable<Boolean>{
            try {
                socket = device?.createRfcommSocketToServiceRecord(
                    UUID.fromString(Const.UUIDS.SPP_UUID)
                )
                if (socket == null) {
                    return@Callable false
                }
                socket!!.connect()
                return@Callable true
            } catch (e: IOException) {
                return@Callable false
            }
        })
        connected = future.get()
        if (!connected) {
            socket = null
            return
        }
        val readFuture = executor.submit(Callable {
            val inputStream = BufferedInputStream(socket?.inputStream)
            val buffer = ByteArray(128)
            try {
                while (true) {
                    val bytes = inputStream.read(buffer, 0, buffer.size)
                    val message = String(buffer.copyOf(bytes))
//                    synchronized(logging) {
//                        when (logging.last().dicrection) {
//                            Direction.TX -> {
//                                logging.plus(transaction(message, Direction.TX))
//                            }
//                            Direction.RX -> {
//                                logging.last().text += message
//                            }
//                        }
//                    }
//                    setChanged()
//                    notifyObservers(message)
                    Logging.receive(message)
                }
            } catch (e: IOException) {

            }
            inputStream.close()
            connected = false
        })
    }

    fun disConnect() {
        deleteObservers()
        if (isconnected()) {
            socket?.close()
        }
        this.socket = null
    }

    fun isconnected() :Boolean {
        return connected
    }

    fun send(message: String) {
        socket?.outputStream?.write((message+CR).toByteArray())
        Logging.send(message)
    }
}
package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.Toast
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Elm327 {
    val executor = Executors.newSingleThreadExecutor()
    val CR = "\n"
    val ATZ = "ATZ"+ CR
    val ATE0 = "ATE0" + CR

    var device: BluetoothDevice? = null
    var socket: BluetoothSocket? = null

    fun init(device : BluetoothDevice, context : Context) {
        if (this.socket != null) {
            this.disConnect()
        }
        val connectFuture = executor.submit(Callable{
            try {
                socket = device?.createRfcommSocketToServiceRecord(
                    UUID.fromString(Const.UUIDS.SPP_UUID)
                )
                if (socket == null) {
                    return@Callable
                }
                socket!!.connect()
                return@Callable
            } catch (e: IOException) {
                return@Callable
            }
        })
        connectFuture.get()
        try {
            if (!waitOk(ATZ) or !waitOk(ATE0)) {
                Toast.makeText(context, "init error", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            this.disConnect()
        }
    }
    fun disConnect() {
        if (this.isConnected()) {
            this.socket?.close()
        }
        this.socket = null
    }
    fun isConnected() : Boolean {
        if (this.socket != null) {
            return this.socket!!.isConnected
        }
        return false
    }

    fun querySingle(pid : Byte, op : Byte) : OBDResponse {
        val command = String.format("02x 02x"+CR, pid, op)
        send(command)
        val reader = BufferedInputStream(this.socket?.inputStream)
        val data= reader.readBytes()
        return OBDResponse(data)
    }
    fun send(message : String) {
        this.socket?.outputStream?.write(message.toByteArray())
        Logging.send(message)
    }

    private fun waitOk(command : String) : Boolean {
        val stream = socket?.inputStream
        val buffer = ByteArray(1024)
        var position = 0
        send(command)
        val waitFuture = stream?.run {
            Logging.send(command)
            executor.submit(Callable<Boolean>{
                try {
                    while (true) {
                        val watchDog = Thread {
                            Thread.sleep(100)
                            throw ErrorResponseException()
                        }.apply {
                            this.run()
                        }
                        val chars = this.read(buffer, position, buffer.size)
                        position += chars
                        watchDog.interrupt()
                        when {
                            buffer.contains('?'.toByte()) -> {
                                Logging.receive(buffer.copyOfRange(0, position).toString())
                                return@Callable false
                            }
                            buffer.toString().toUpperCase().contains("OK") -> {
                                Logging.receive(buffer.copyOfRange(0, position).toString())
                                return@Callable true
                            }
                        }
                    }
                } catch (e: Exception) {
                }
                if (position != 0) {
                    Logging.receive(buffer.copyOfRange(0, position).toString())
                }
                return@Callable false
            })
        }
        return waitFuture!!.get()
    }

    private class ErrorResponseException : Exception () {

    }
}
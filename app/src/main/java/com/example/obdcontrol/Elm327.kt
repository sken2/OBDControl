package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.regex.Pattern

object Elm327 {
    val executor = Executors.newSingleThreadExecutor()
    val CR = "\r"
    private val LF = "\n"
    val ATZ = "ATZ"
    val ATE0 = "ATE0"
    val READ_TIMEOUT : Long = 200  //

    private var socket: BluetoothSocket? = null
    private var appContext : Context? = null
    private var readFutuer : Future<Boolean>? = null

    private val simpleResonse = Pattern.compile("[0-9A-F]{2,3}")
    private var delimiter = CR

    fun init(device : BluetoothDevice, context : Context) {
        this.socket?.run {
            disConnect()
        }
        this.appContext = context.applicationContext
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
            if (waitOk(ATZ) and waitOk(ATE0)) {
                Toast.makeText(this.appContext, "init error", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            this.disConnect()
        }
        readFutuer = executor.submit(ReadTask())
    }

    fun disConnect() {
        if (this.isConnected()) {
            this.socket?.close()
            readFutuer?.run {
                this.cancel(true)
            }
        }
        this.socket = null
        readFutuer = null
    }

    fun isConnected() : Boolean {
        this.socket?.run {
            return this.isConnected
        }
        return false
    }

    fun send(message : String) {
        this.socket?.outputStream?.write((message + delimiter).toByteArray())
        Logging.send(message + LF)
    }

    fun toBytes(command : String) : Array<Byte> {
        var data = emptyArray<Byte>()
        val hexStrs = command.split(" ")
        hexStrs.forEach {
            if (it.length >= 2) {
                val chars = it.toCharArray()
                val digith = Character.digit(chars[0], 16)
                val digitl = Character.digit(chars[1], 16)
                data += (digith * 16 + digitl).toByte()
            }
        }
        return data
    }

    private fun waitOk(command : String) : Boolean {
        val stream = socket?.inputStream
        val buffer = ByteArray(1024)
        var position = 0
        send(command)
        val waitFuture = stream?.run {
            executor.submit(Callable<Boolean>{
                try {
                    while (true) {
                        val watchDog = Thread {
                            Thread.sleep(READ_TIMEOUT)
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
        waitFuture?.run {
            return this.get()
        }
        Log.e(Const.TAG, "Elm327::init future is null")
        return false
    }

    object Monitor : Observable() {
        var readTask : Future<Boolean>? = null
        fun start() {
            readTask = executor.submit(ReadTask())
        }

        fun stop() {
            readTask?.run {
                this.cancel(true)
            }
        }

        fun isRunning() : Boolean {
            readTask?.run {
                return !this.isDone
            }
            return false
        }

        fun arriveed(message : OBDResponse) {
            setChanged()
            notifyObservers(message)
        }
    }

    private class ReadTask() : Callable<Boolean> {
        val stream = socket?.inputStream
        override fun call(): Boolean {
            try {
                val reader = BufferedReader(InputStreamReader(stream))
                while(true) {
//                    val watchDog = Thread {
//                        Thread.sleep(READ_TIMEOUT)
//                        println("bow wow")
//                        throw ErrorResponseException()
//                    }.apply {
//                        this.run()
//                    }
                    val response = reader.readLine()
                    Logging.receive(response + LF)
                    val obdResponse = OBDResponse(response).apply {
                        if (this.isValid) {
                            Monitor.arriveed(this)
                        }
                    }
                }
            } finally {
                stream?.close()
            }
        }
    }

    private class ErrorResponseException : Exception () {

    }
}
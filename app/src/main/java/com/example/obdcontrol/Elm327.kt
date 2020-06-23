package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.Future

object Elm327 {
    private val executor = Executors.newSingleThreadExecutor()
    private val CR = "\r"
    private val LF = "\n"
    private val ATZ = "ATZ"
    const val ATE0 = "ATE0"
    const val ATH1 = "ATH1"
    const val READ_TIMEOUT : Long = 500  //

    private var socket: BluetoothSocket? = null
    private var readFutuer : Future<Boolean>? = null

    private var delimiter = CR

//    private var canSpeed = CAN_SPEED.AUTO
    private var canSpeed = CAN_SPEED.ISO15765_4_11bits  // for my car // TODO

    fun init(device : BluetoothDevice, context : Context) {
        this.socket?.run {
            disConnect()
        }
        val connectFuture = executor.submit {
            try {
                socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString(Const.UUIDS.SPP_UUID)
                )
                if (socket == null) {
                    return@submit
                }
                socket!!.connect()
                return@submit
            } catch (e: IOException) {
                Log.e(Const.TAG, "Elm327::init ${e.message}")
            }
        }
        connectFuture.get()
        try {
            val result =
                waitOk(ATZ) and
                waitOk(ATE0) and
                waitOk(ATH1)// and
//                waitOk("AT SP${canSpeed.speed}")
            if(!result) {
                Looper.prepare()
                Toast.makeText(context, "init error", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            this.disConnect()
        }
        readFutuer = executor.submit(ReadTask())
    }

    fun setSpeed(speed : Elm327.CAN_SPEED) {
        this.canSpeed = speed
    }
    fun getSpeed() : Elm327.CAN_SPEED {
        return this.canSpeed
    }

    fun disConnect() {
        if (Monitor.isRunning()) {
            Monitor.stop()
        }
        if (this.isConnected()) {
            readFutuer?.run {
                try {
                    this.cancel(true)
                    this.get()
                } catch (e : CancellationException) {
                }
            }
            waitOk("ATLP")
            this.socket?.close()
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
        try {
            this.socket?.outputStream?.write((message + delimiter).toByteArray())
        } catch (e : IOException) {

        } finally {
            Logging.send(message + LF)
        }
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

    fun saveTo(stream : OutputStream) : Boolean {
        return Logging.save(stream)
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
                        Logging.receive(buffer.copyOfRange(0, position).toString())
                        when {
                            buffer.contains('?'.toByte()) -> {
                                return@Callable false
                            }
                            buffer.toString().toUpperCase(Locale.ROOT).contains("OK") -> {
                                return@Callable true
                            }
                        }
                    }
                } catch (e: Exception) {
                }
                if (position != 0) {
                    Logging.receive(buffer.copyOfRange(0, position).toString() + "+")   //mark timeout with "+"
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
        var monitoring = false

        fun start() {
            monitoring = true
            send("ATMA")
            setChanged()
            notifyObservers()
        }

        fun stop() {
            monitoring = false
            if (isConnected()) {
                send(" ")
            }
//            setChanged()
//            notifyObservers()
        }

        fun isRunning() : Boolean {
            return monitoring
        }

        fun arriveed(message : OBDResponse) {
            if (monitoring) {
                setChanged()
                notifyObservers(message)
            }
        }
    }

    private class ReadTask() : Callable<Boolean> {
        val stream = socket?.inputStream
        override fun call(): Boolean {
            try {
                stream?.run {
                    val scanner = Scanner(this).apply {
                        useDelimiter(CR)
                    }
                    while (!Thread.interrupted()) {
                        val response = scanner.next()
                        Logging.receive(response + "$" + LF)//TODO remove "$" someday
                        val obdResponse = OBDResponse(response, false).apply {
                            if (this.isValid()) {
                                Monitor.arriveed(this)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(Const.TAG, "Elm327::ReadTask ${e.message}")
            } finally {
                stream?.close()
            }
            return false
        }
    }

    private class ErrorResponseException : Exception () {

    }

    enum class CAN_SPEED(val speed : String) {
        AUTO("0"),
        SAE_J1850_PWM("1"),
        SAE_J1850_VPW("2"),
        ISO9142_2("3"),
        ISO14230_4_SLOW("4"),
        ISO14230_4("5"),
        ISO15765_4_11bits("6"),
        ISO15765_4_29bits("7"),
        ISO15765_4_11bits_500k("8"),
        ISO15765_4_29bits_500k("9"),
        USER1("A"),
        USER2("B")
    }
}
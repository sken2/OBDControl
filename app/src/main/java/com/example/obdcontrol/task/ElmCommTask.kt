package com.example.obdcontrol.task

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.obdcontrol.BadAccidentDisposer
import com.example.obdcontrol.Const
import com.example.obdcontrol.Elm327
import com.example.obdcontrol.Logging
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ElmCommTask : Service() {

    private val executor = Executors.newSingleThreadExecutor()
    private var socket: BluetoothSocket? = null
    private var disposer : BadAccidentDisposer? = null
    private var openFuture : Future<BluetoothSocket>? = null
    private var initFuture : Future<Boolean>? = null
    private val handler = Handler(Looper.getMainLooper())

    lateinit var delimiter : String

    override fun onCreate() {
        Log.v(Const.TAG, "ElmCommTask::onCreate")
        super.onCreate()
        this.delimiter = Const.CR //TODO create setter
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(Const.TAG, "ElmCommTask::onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.v(Const.TAG, "ElmCommTask::onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        socket?.run {
            Log.e(Const.TAG, "ElmCommTask::onBind already binded")
            return null
        }
        intent?.let {
            val deviceInExtra = it.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_NAME)
            deviceInExtra?.run {
                Log.i(Const.TAG, "ElmCommtask::onBind binded for ${this.name}")
                openFuture = executor.submit(OpenTask(this, this@ElmCommTask))
                return binder
            }
        }
        Log.e(Const.TAG, "ElmCommTask::onBind no device in the intent")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        socket?.run {
            closeComm()
        }
        socket = null
        disposer = null
        return super.onUnbind(intent)
    }

    fun closeComm() {
        socket?.run {
            try {
                close()
            } catch (e : Exception) {
                disposer?.dispose(e)
            }
        }
    }

    fun onOpenConnection() {
        openFuture?.run {
            try {
                socket = this.get()
                socket?.let {
                    initFuture = executor.submit(InitTask(it, this@ElmCommTask))
                }
            } catch (e : Exception) {
                disposer?.dispose(e)
            }
        }
    }

    fun onInitializeDevice() {

    }

    fun setDisposer(disposer: BadAccidentDisposer) {
        this.disposer = disposer
    }

    private fun getInitializeCommands() : List<String>{
        return listOf("ATE0", "ATH1")
    }

    private fun send(message : String) {
        try {
            this.socket?.outputStream?.write((message + delimiter).toByteArray())
        } catch (e : IOException) {

        } finally {
            Logging.send(message + Const.LF)
        }
    }

    private fun waitOk(stream : InputStream, expect : String = "OK" + delimiter) : Boolean{
        val buffer = ByteArray(1024)
        var position = 0
        try {
            while (true) {
                val watchDog = Thread {
                    Thread.sleep(500)
                }.apply {
                    this.run()
                }
                val chars = stream.read(buffer, position, buffer.size)
                position += chars
                watchDog.interrupt()
                Logging.receive(buffer.copyOfRange(0, position).toString())
                when {
                    buffer.contains('?'.toByte()) -> {
                        return false
                    }
                    buffer.toString().toUpperCase(Locale.ROOT).contains(expect) -> {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            disposer?.dispose(e)
        }
        return false
    }

    class OpenTask(private val device : BluetoothDevice, private val service : ElmCommTask) : Callable<BluetoothSocket> {

        override fun call() : BluetoothSocket? {
            try {
                return device.createRfcommSocketToServiceRecord(UUID.fromString(Const.UUIDS.SPP_UUID))
            } catch (e : Exception) {
                service.disposer?.dispose(e)
                return null
            } finally {
                service.handler.post{
                    service.onOpenConnection()
                }
            }
        }
    }

    class InitTask(private val socket : BluetoothSocket, private val service: ElmCommTask) : Callable<Boolean> {

        var initializeSuccess = true

        override fun call(): Boolean {
            val stream = socket.inputStream
            service.send("ATZ")
            initializeSuccess = service.waitOk(stream, Const.CR + Const.CR)
            service.getInitializeCommands().forEach {
                service.send(it)
                initializeSuccess = initializeSuccess and service.waitOk(stream)
            }
            service.handler.post {
                service.onInitializeDevice()
            }
            return initializeSuccess
        }
    }

    inner class LocalBinder() : Binder() {
        fun getService() : ElmCommTask {
            return this@ElmCommTask
        }
    }
    val binder = LocalBinder()

    private fun init() {
        //TODO
    }
}
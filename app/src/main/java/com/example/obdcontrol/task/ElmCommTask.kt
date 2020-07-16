package com.example.obdcontrol.task

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.obdcontrol.BadAccidentDisposer
import com.example.obdcontrol.Const
import com.example.obdcontrol.entities.Logging
import com.example.obdcontrol.R
import com.example.obdcontrol.setup.BluetoothSetup
import java.io.BufferedInputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.*
import kotlin.NoSuchElementException

class ElmCommTask : Service(), Observer {

    private val executor = Executors.newSingleThreadExecutor()
    private var socket: BluetoothSocket? = null
    private var disposer : BadAccidentDisposer? = null
    private var openFuture : Future<BluetoothSocket>? = null
    private var initFuture : Future<Boolean>? = null
    private var listenFuture : Future<Unit>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var listener : ConnectionStateListener? = null
    lateinit var delimiter : String
    private var deviceName = ""
    private val preferece by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate() {
        Log.v(Const.TAG, "ElmCommTask::onCreate")
        super.onCreate()
        this.delimiter = Const.CR //TODO create setter
        this.disposer = ideDebuggingDisposer
        Monitor.init(this)
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
                this@ElmCommTask.deviceName = this.name
                openFuture = executor.submit(OpenTask(this, this@ElmCommTask))
                return binder
            }
        }
        Log.e(Const.TAG, "ElmCommTask::onBind no device in the intent")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v(Const.TAG, "ElmCommTask::onUnbind")
        openFuture?.cancel(true)
        initFuture?.cancel(true)
        listenFuture?.run {
            stopListen()
        }
        closeComm()
        disposer = null
        return super.onUnbind(intent)
    }

    private fun closeComm() {
        socket?.also {
            try {
                if (it.isConnected()) {
                    it.close()
                }
            } catch (e : Exception) {
                disposer?.dispose(e)
            } finally {
                socket = null
            }
        }
    }

    fun setDisposer(disposer: BadAccidentDisposer) {
        this.disposer = disposer
    }

    fun setConnectionStateListener(listener : ConnectionStateListener?) {
        if (listener != null) {
            this.listener = WeakReference(listener).get()
        } else {
            this.listener = null
        }
    }

    fun isConnected() : Boolean {
        return socket != null
    }

    protected fun onOpenConnection() {
        openFuture?.run {
            try {
                socket = this.get()
                socket?.let {
                    BluetoothSetup.addObserver(this@ElmCommTask)
                    listener?.onConnectionOpened()
                    initFuture = executor.submit(InitTask(this@ElmCommTask))
                }
            } catch (e : CancellationException) {
                Log.i(Const.TAG, "ElmCommTask::onOpenConnection : canceled")
            }catch (e : Exception) {
                disposer?.dispose(e)
            }
        }
        openFuture = null
    }

    protected fun onDeviceInitialized() {
        try {
            val result = initFuture?.get()
            Toast.makeText(this, "Initialize complete $result", Toast.LENGTH_SHORT).show()
            startListen()
        } catch (e : CancellationException) {
            Log.i(Const.TAG, "ElmCommTask::onInitializeDevice : canceled")
        } catch (e : Exception) {
            disposer?.dispose(e)
        } finally {
            listener?.onConectionInitialized()
        }
        initFuture = null
    }

    fun startListen() {
        Log.v(Const.TAG, "ElmCommTask::startListen")
        socket?.let {
            listenFuture = executor.submit(MonitorTask(this@ElmCommTask))
        }
    }

    fun stopListen() {
        Log.v(Const.TAG, "ElmCommTask::stopListen")
        listenFuture?.run {
            cancel(true)
            try {
                get()
            } catch (e : CancellationException) {
                //
            } catch (e : java.lang.Exception) {
                disposer?.dispose(e)
            } finally {
                listenFuture = null
            }
        }
    }

    private fun onConnectionDisconnected(e : Exception? = null) {
        Log.i(Const.TAG, "ElmCommTask::onConnectionDisconnected ${e?.message}")
        listener?.onConnectionClosed()
        listener = null
    }

    private fun getInitializeCommands() : List<String>{
        val initCommands = preferece.getString(
            getString(R.string.edit_initial_commands),
            "ATZ\nATE0\nATH1\nATSP6") as String
        return initCommands.split("\n")
    }

    fun send(message : String) {
        try {
            socket?.outputStream?.write((message+delimiter).toByteArray())
        } catch (e : IOException) {
            onConnectionDisconnected(e)
        } finally {
            Logging.send(message + Const.LF)
        }
    }

    private fun waitOk(expect : String = ">") : Boolean{
        val buffer = ByteArray(1024)
        var position = 0
        socket?.inputStream?.run {
            BufferedInputStream(this).run {
                try {
                    var count = 0
                    while (!Thread.interrupted()) {
                        Thread.sleep(10)
                        val chars = read(buffer, position, this.available())
                        position += chars
                        val msgInBuffer = buffer.copyOfRange(0, position)
                        Log.d(Const.TAG, "ElmCommTask::waitOk read chars = $chars buffer = ${msgInBuffer.toString(Charset.defaultCharset())} ")
                        if (buffer.copyOfRange(0, position).toString(Charset.defaultCharset()).indexOf(expect) != -1) {
                            break
                        }
                        if (count++ > 10) {
                            break
                        }
                    }
                    val response = buffer.copyOfRange(0, position).toString(Charset.defaultCharset())
                    Logging.receive(response)
                    when {
                        response.indexOf("?") != -1-> {
                            return false
                        }
                        response.indexOf("OK") != -1-> {
                            return true
                        }
                        else -> {
                            Log.e(Const.TAG, "ElmCommTask::waitOk no response identifier")
                            return false
                        }  // something wrong
                    }
                } catch (e : TimeoutException) {
                    //
                } catch (e : IOException) {
                    onConnectionDisconnected(e)
                } catch (e: Exception) {
                    disposer?.dispose(e)
                }
            }
        }
        if (position != 0) {
            Logging.receive(buffer.copyOfRange(0, position).toString(Charset.defaultCharset()) + "+")   //mark timeout with "+"
        }
        return false
    }

    object Monitor : Observable() {

        private var running = false
        lateinit var service: ElmCommTask

        fun init (service : Context) {
            if (service is ElmCommTask) {
                running = false
                this.service = service
            }
        }

        fun start (o : Observer, command : String) {
            running = true
            addObserver(o)
            service.send(command)
        }

        fun stop () {
            deleteObservers()
            running = false
        }

        fun isRunning() : Boolean {
            return running
        }

        fun arrive(message : String) {
            when (message.trim()) {
                "?", "NO DATA", "BUS ERROR", "STOPPED" -> {
                    stop()
                }
                "OK" -> {}
            }
            setChanged()
            notifyObservers()
        }
    }

    private class OpenTask(private val device : BluetoothDevice, private val service : ElmCommTask) : Callable<BluetoothSocket> {

        override fun call() : BluetoothSocket? {
            try {
                val socket =
                    device.createRfcommSocketToServiceRecord(UUID.fromString(Const.UUIDS.SPP_UUID))
                socket?.connect()
                return socket
            } catch (e : IOException) {
                service.onConnectionDisconnected(e)
            } catch (e : Exception) {
                service.disposer?.dispose(e)
            } finally {
                service.handler.post{
                    service.onOpenConnection()
                }
            }
            return null
        }
    }

        private class InitTask(private val service: ElmCommTask) : Callable<Boolean> {

            var initializeSuccess = true

            override fun call(): Boolean {
                try {
                    service.socket?.inputStream?.apply {
                        service.send("ATZ")
                        Thread.sleep(2000)
                        service.waitOk()
                        service.getInitializeCommands().forEach {
                            service.send(it)
                            val result = service.waitOk()
                            initializeSuccess = initializeSuccess and result
                        }
                    }
                } catch (e : IOException) {
                    service.onConnectionDisconnected(e)
                } catch (e : Exception) {
                    service.disposer?.dispose(e)
                } finally {
                    service.handler.post{
                        service.onDeviceInitialized()
                    }
                    return initializeSuccess
                }
            }
        }

        private class MonitorTask(val server : ElmCommTask) : Callable<Unit>{

            val scanner by lazy {
                server.socket?.inputStream?.let {
                    Scanner(BufferedInputStream(it)).apply {
                        useDelimiter(Const.CR)
                    }
                }
            }

        override fun call(): Unit {
            try {
                scanner?.run {
                    while (!Thread.interrupted()) {
                        val message = next()
                        if (message == null) {
                            break
                        }
                        if (Monitor.isRunning()) {
                            Monitor.arrive(message)
                        }
                        Logging.receive(message)
                    }
                }
            } catch (e : IOException) {
                server.onConnectionDisconnected(e)
            } catch (e : NoSuchElementException) {
                Log.v(Const.TAG, "ElmCommTask::MonitorTask", e)
                server.onConnectionDisconnected(e)
            } catch (e : Exception) {
                server.disposer?.dispose(e)
            }
            return
        }
    }

    inner class LocalBinder() : Binder() {
        fun getService() : ElmCommTask {
            return this@ElmCommTask
        }
    }
    val binder = LocalBinder()

    interface ConnectionStateListener {
        fun onConnectionOpened() {}
        fun onConectionInitialized() {}
        fun onConnectionClosed() {}
    }

    override fun update(o: Observable?, arg: Any?) {
        TODO("Not yet implemented")
    }

    val ideDebuggingDisposer = object : BadAccidentDisposer {
        override fun dispose(e: Exception) {
            e.printStackTrace()
        }
    }
}
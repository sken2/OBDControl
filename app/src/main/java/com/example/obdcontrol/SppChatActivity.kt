package com.example.obdcontrol

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.*

class SppChatActivity : AppCompatActivity() {

    private var device : BluetoothDevice? = null
    private val logBox : TextView by lazy {
        findViewById(R.id.chat_log) as TextView
    }
    private val sayBox : EditText by lazy {
        findViewById(R.id.chat_say) as EditText
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spp_chat)
        device =
            if (savedInstanceState != null)
                savedInstanceState.getParcelable<BluetoothDevice>("device")
            else {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
            }
        if (device == null) {
            Toast.makeText(this, "No Device", Toast.LENGTH_SHORT).show()
            finishActivity(255)
        }
        sayBox.setOnEditorActionListener(editWatcher)
        if (!Elm327.isConnected()) {
            Toast.makeText(this, "Connect Failed", Toast.LENGTH_SHORT).show()
            finishActivity(255)
        }
    }

    override fun onResume() {
        super.onResume()
        Logging.addObserver(rxObserver)
    }

    override fun onPause() {
        Logging.deleteObserver(rxObserver)
        super.onPause()
    }

    override fun onDestroy() {
        Elm327.disConnect()
        super.onDestroy()
    }

    private val editWatcher = object :TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (v != null) {
                    val text = v.text
                    println(text)
                    Elm327.send(text.toString())
                    return true
                }
            }
            return false
        }
    }

    private val rxObserver = object : Observer {
        override fun update(o: Observable?, arg: Any?) {
            if (o is Logging) {
                runOnUiThread {
                    logBox.text = (o as Logging).getMessage()
                }
            }
        }
    }
}

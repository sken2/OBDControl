package com.example.obdcontrol

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.DialogFragment
import java.util.*

class SppChatActivity : AppCompatActivity() {

    private var device : BluetoothDevice? = null
    private val logBox : TextView by lazy {
        findViewById(R.id.chat_log) as TextView
    }
    private val sayBox : EditText by lazy {
        findViewById(R.id.chat_say) as EditText
    }
    private val button1 : Button by lazy {
        findViewById(R.id.button) as Button
    }
    private val button2 : Button by lazy {
        findViewById(R.id.button2) as Button
    }
    private val button3 : Button by lazy {
        findViewById(R.id.button3) as Button
    }
    private val preference by lazy {
        this.applicationContext.getSharedPreferences(Const.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spp_chat)
        device =
            if (savedInstanceState != null)
                savedInstanceState.getParcelable("device")
            else {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
            }
        if (device == null) {
            Toast.makeText(this, "No Device", Toast.LENGTH_SHORT).show()
            finishActivity(255)
        }
        preference.edit().putString(Const.Preference.PREF_DEVICE, device?.address).apply()
        sayBox.setOnEditorActionListener(editWatcher)
        logBox.movementMethod = ScrollingMovementMethod()
        if (!Elm327.isConnected()) {
            Toast.makeText(this, "Connect Failed", Toast.LENGTH_SHORT).show()
            finishActivity(255)
        }
        button1.setOnClickListener { v ->
            Elm327.send((v as Button).text.toString())
        }
        button2.setOnClickListener { v ->
            Elm327.send((v as Button).text.toString())
        }
        button3.setOnClickListener {
            Elm327.Monitor.start()
        }
        button1.setOnLongClickListener {v ->
            ButtonPresetDialog(v as Button, Const.Keys.Preset1).show(supportFragmentManager, "Preset ")
            button1.text = preference.getString(Const.Keys.Preset1, "ATZ")
            true
        }
        button2.setOnLongClickListener {v ->
            ButtonPresetDialog(v as Button, Const.Keys.Preset2).show(supportFragmentManager, "Preset ")
            button2.text = preference.getString(Const.Keys.Preset2, "ATMA")
            true
        }
        button1.text = preference.getString(Const.Keys.Preset1, "ATZ")
        button2.text = preference.getString(Const.Keys.Preset2, "ATMA")
    }

    override fun onResume() {
        super.onResume()
        logBox.text = Logging.getMessage()
        Logging.addObserver(rxObserver)
        Elm327.Monitor.addObserver(obdObserver)
    }

    override fun onPause() {
        Logging.deleteObserver(rxObserver)
        Elm327.Monitor.deleteObserver(obdObserver)
        Elm327.Monitor.stop()
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
                    Log.v(Const.TAG, "SppChatActivity::editWatcher $text")
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
                    logBox.text = o.getMessage()
                }
            }
        }
    }

    private val obdObserver = object : Observer {
        override fun update(o: Observable?, arg: Any?) {
            if (arg is OBDResponse) {
                Log.v(Const.TAG, "SppChatActivity::obdObserver $arg")
            }
        }
    }

    class ButtonPresetDialog(button : Button, key : String) : DialogFragment() {
        private val key = key
        private val button = button
        val preference : SharedPreferences by lazy {
            this.context!!.getSharedPreferences(Const.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val presetText = preference.getString(key, "")
            val editText = EditText(activity).apply {
                setText(presetText)
            }
             return AlertDialog.Builder(activity)
                .setView(editText)
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->  })
                .setPositiveButton("Ok") { dialog, witch ->
                    preference.edit().putString(key, editText.text.toString()).apply()
                    button.text = editText.text
                    Toast.makeText(activity, editText.text, Toast.LENGTH_SHORT).show()
                }
                .create()
        }
    }
}

package com.example.obdcontrol

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
    private val clearButton : ImageButton by lazy {
        findViewById(R.id.btn_clear) as ImageButton
    }
    private val saveButton : ImageButton by lazy {
        findViewById(R.id.btn_save) as ImageButton
    }
    private val SAVE_REQUEST = 1234

    private val preference by lazy {
        this.applicationContext.getSharedPreferences(Const.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    private var monitorIsActive = false //超ダサいがcompanionは使いたくない

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
        button3.setOnClickListener { v ->
            monitorIsActive = monitorButton(v as Button, monitorIsActive)
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
        saveButton.setOnClickListener{button ->
            val fileName = createFile()
            val request = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("taxt/plain")
                .putExtra(Intent.EXTRA_TITLE, fileName)
            startActivityForResult(request, SAVE_REQUEST)
        }
        button1.text = preference.getString(Const.Keys.Preset1, "ATZ")
        button2.text = preference.getString(Const.Keys.Preset2, "ATMA")

        Elm327.Monitor.addObserver(obdObserver)
    }

    override fun onResume() {
        super.onResume()
        Logging.addObserver(rxObserver)
        logBox.text = Logging.getMessage()
        this.monitorIsActive = Elm327.Monitor.isRunning()
        if (monitorIsActive) {
            button3.text = getString(R.string.btn_running)

        } else {
            button3.text = getString(R.string.btn_stopped)
        }
    }

    override fun onPause() {
        Logging.deleteObserver(rxObserver)
        super.onPause()
    }

    override fun onDestroy() {
        Elm327.Monitor.deleteObserver(obdObserver)
        Elm327.disConnect()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }
        Log.v(Const.TAG, "SppChatActivity::onActivityResult request = $requestCode action = ${data.action} result = $resultCode")
        when(requestCode) {
            SAVE_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    data.data?.run {
                        val stream = contentResolver.openOutputStream(this)
                        stream?.run {
                            if (!Elm327.saveTo(this)) {
                                Toast.makeText(this@SppChatActivity, "OOPS", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun monitorButton(button : Button, isActive : Boolean) : Boolean {
        if (isActive) {
            Elm327.Monitor.stop()
            button.text = getString(R.string.btn_stopped)
        } else {
            Elm327.Monitor.start()
//            Elm327.send("ATMA")
            button.text = getString(R.string.btn_running)
        }
        return !isActive
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

    private fun createFile() : String {
        val now = Date()//TODO
        return "Elm327.txt"
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

    class ButtonPresetDialog(val button : Button, val key : String) : DialogFragment() {
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

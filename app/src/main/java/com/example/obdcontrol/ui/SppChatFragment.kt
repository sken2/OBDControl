package com.example.obdcontrol.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.obdcontrol.Const
import com.example.obdcontrol.Elm327
import com.example.obdcontrol.Logging
import com.example.obdcontrol.R

class SppChatFragment : Fragment() {

    private var monitorStarted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(Const.TAG, "SppChatFragment::onCreateView")
        return inflater.inflate(R.layout.fragment_spp_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "SppChatFragment::onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_show_log).apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_sppChatFragment_to_loggingFragment)
            }
        }
        view.findViewById<Button>(R.id.button_start_monitor).apply {
            setOnClickListener {
                val button= it as Button
                with(activity) {
                    if (this is StartupActivity) {
                        when (monitorStarted) {
                            false -> {
                                StartMonitorDialog(it, {
                                    this@apply.text = "Monitor Running"
                                    this.service?.send(it)
                                    monitorStarted = true
                                }).show(supportFragmentManager, "monitor")
                            }
                            true -> {
                                it.text = "Monitor"
                                this.service?.send(" ")
                                monitorStarted = false
                            }
                        }
                    }
                }
//                Toast.makeText(context, "MONITOR, Comming soon!", Toast.LENGTH_SHORT).show()//TODO
            }
        }
        val edit = view.findViewById<EditText>(R.id.edit_command).apply {
//            setOnEditorActionListener(editWatcher)
        }
        view.findViewById<ImageButton>(R.id.button_send_message).apply {
            setOnClickListener{
                if (context is StartupActivity) {
                    val activity = context as StartupActivity
                    activity.service?.send(edit.text.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        Log.v(Const.TAG, "SppChatFragment::onDestroyView")
        if (monitorStarted) {
            with(activity) {
                if (this is StartupActivity) {
                    this.service?.send(" ")
                }
            }
        }
        monitorStarted = false
        super.onDestroyView()
    }

    class StartMonitorDialog(val button : Button, val callback : (command : String) ->Unit) : DialogFragment() {

        val editText by lazy {
            EditText(activity).apply {
                setText("AT MA")
            }
        }
        // TODO preset buttons of reciever and transmitter
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity)
                .setView(editText)
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->  })
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, witch ->
                    callback.invoke(editText.text.toString())
                })
                .create()
        }
    }

    private val editWatcher = object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    v?.run {
                        Log.v(Const.TAG, "SppChatFragment::editWatcher $text")
                        if (context is StartupActivity) {
                            val activity = context as StartupActivity
                            activity.service?.send(text.toString())
                        }
                        return true
                    }
                }
            }
            return false
        }
    }
}
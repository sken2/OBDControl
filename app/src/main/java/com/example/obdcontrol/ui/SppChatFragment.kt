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
                val botton= it as Button
                with(activity) {
                    if (this is StartupActivity) {
                        when (monitorStarted) {
                            true -> {
                                StartMonitorDialog(it).show(supportFragmentManager, "monitor")
                                it.text = "Monitor"
                                this.service?.send("ATMA")
                            }
                            false -> {
                                it.text = "Stop Monitor"
                                this.service?.send(" ")
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
        super.onDestroyView()
    }

    class StartMonitorDialog(val button : Button) : DialogFragment() {

        val startupActivity : StartupActivity = activity as StartupActivity //!!
        val editText = EditText(activity).apply {
            setText("AT MA")
        }
        // TODO preset buttons of reciever and transmitter
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity)
                .setView(editText)
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->  })
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, witch ->
                    startupActivity.service?.send(editText.text.toString())
                    button.text = "Monitor Running"
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
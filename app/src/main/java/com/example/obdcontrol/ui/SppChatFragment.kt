package com.example.obdcontrol.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.Logging
import com.example.obdcontrol.R
import com.example.obdcontrol.adapters.CommandHistoryAdapter
import com.example.obdcontrol.adapters.CommandHistoryLayoutManager

class SppChatFragment : Fragment() {

    private val historyAdapter = CommandHistoryAdapter()
    private lateinit var recyclerView: RecyclerView
    private val commandEdittext by lazy {
        view?.findViewById<EditText>(R.id.edit_command)
    }

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

        recyclerView = view.findViewById(R.id.recycler_command_history)
        view.findViewById<Button>(R.id.button_show_log).apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_sppChatFragment_to_loggingFragment)
            }
        }
        view.findViewById<Button>(R.id.button_start_monitor).apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_sppChatFragment_to_monitoringFragment)
            }
        }
        view.findViewById<ImageButton>(R.id.button_send_message).apply {
            setOnClickListener{
                if (context is StartupActivity) {
                    val activity = context as StartupActivity
                    activity.service?.send(commandEdittext?.text.toString())
                    historyAdapter.issue(commandEdittext?.text.toString())
                }
            }
        }
        recyclerView.apply {
            layoutManager = CommandHistoryLayoutManager()
            adapter = historyAdapter
        }
        view.findViewById<TextView>(R.id.text_dialog).apply {
            setOnClickListener {
                this.text = Logging.getMessage()
            }
        }
    }

    override fun onDestroyView() {
        Log.v(Const.TAG, "SppChatFragment::onDestroyView")
        recyclerView.adapter = null
        super.onDestroyView()
    }

    val actionObserver = object : SelectionTracker.SelectionObserver<String>() {

        override fun onSelectionChanged() {
            Log.v(Const.TAG, "SppChatFragment::onSelectionChanged")
            super.onSelectionChanged()
            if (historyAdapter.selectionTracker.hasSelection()) {
                commandEdittext?.run {
                    val command = historyAdapter.selectionTracker.selection.first()
                    commandEdittext?.setText(command, TextView.BufferType.EDITABLE)
                }
            }
        }
    }

    class StartMonitorDialog(val button : Button, val callback : (command : String) ->Unit) : DialogFragment() {

        val editText by lazy {
            EditText(activity).apply {
                setText("AT MT C0")
            }
        }
        // TODO preset buttons for reciever and transmitter
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
package com.example.obdcontrol.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.entities.SetupActionModeCallback
import com.example.obdcontrol.task.ElmCommTask
import kotlinx.android.synthetic.main.fratment_monitoring.view.*
import java.util.*

class MonitoringFragment : Fragment(), Observer {

    private val startupActivity by lazy {
        requireActivity() as StartupActivity
    }
    private val pid by lazy {
        view?.findViewById<EditText>(R.id.edit_filter_pid)
    }
    private val loggingText by lazy {
        view?.findViewById<TextView>(R.id.text_dialog)
    }
    private var running = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fratment_monitoring, container, false)
    }
    var monitorLogging : String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterBuilder.pid = pid!!
        with(view) {
            findViewById<Button>(R.id.button_start_monitor).run {
                setOnClickListener {
                    refrectState()
                    if (running) {
                        startupActivity.service?.run {
                            ElmCommTask.Monitor.stop()
                        }
                    } else {
                        monitorLogging = ""
                        val  command = filterBuilder.getCommand()
                        startupActivity.service?.run {
                            ElmCommTask.Monitor.start(this, command)
                        }
                    }
                    running = !running
                }
            }
            findViewById<RadioGroup>(R.id.group_choose_filter)?.run {
                setOnCheckedChangeListener(filterBuilder)
            }
            findViewById<ScrollView>(R.id.scroll_logging).run {
                // TODO set scoller
            }
            loggingText?.customInsertionActionModeCallback = SetupActionModeCallback(requireContext().applicationContext)
        }
    }

    override fun onResume() {
        super.onResume()
        refrectState()
    }

    override fun update(o: Observable?, arg: Any?) {
//        Log.v(Const.TAG, "Monitoringragment::update")
        if (o is ElmCommTask.Monitor) {
            monitorLogging += "\n" + arg as String
            loggingText?.text = monitorLogging
        }
    }

    private fun refrectState() {
        view?.findViewById<Button>(R.id.button_start_monitor)?.run {
            if (running) {
                text = getString(R.string.btn_running)
            } else {
                text = getString(R.string.btn_stopped)
            }
        }
    }

    private object filterBuilder : RadioGroup.OnCheckedChangeListener {
        lateinit var pid : EditText
        var sendPid : String = "C1"
        var receievePid : String = "00"
        var lastSelection = R.id.radio_filter_none

        @SuppressLint("SetTextI18n")
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                R.id.radio_filter_none -> {
                    pid.isEnabled = false
                    lastSelection = R.id.radio_filter_none
                }
                R.id.radio_receiver_filter -> {
                    pid.isEnabled = true
                    if (lastSelection == R.id.radio_tranfsmitter_filter) {
                        sendPid = pid.text.toString()
                    }
                    pid.setText(receievePid, TextView.BufferType.EDITABLE)
                    lastSelection = R.id.radio_receiver_filter
                }
                R.id.radio_tranfsmitter_filter -> {
                    pid.isEnabled = true
                    if (lastSelection == R.id.radio_receiver_filter) {
                        receievePid = pid.text.toString()
                    }
                    pid.setText(sendPid, TextView.BufferType.EDITABLE)
                    lastSelection = R.id.radio_tranfsmitter_filter
                }
            }
        }

        fun getCommand() : String {
            when (lastSelection) {
                R.id.radio_filter_none -> {
                    return "ATMA"
                }
                R.id.radio_receiver_filter -> {
                    return "AT MR ${pid.text}"
                }
                R.id.radio_tranfsmitter_filter -> {
                    return "AT MT ${pid.text}"
                }
            }
            // never come to here, i hope
            Log.e(Const.TAG, "MonitoringFragment::getCommands unknown error")
            return ""
        }
    }
}
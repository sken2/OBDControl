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
import com.example.obdcontrol.task.ElmCommTask
import java.util.*

class MonitoringFragment : Fragment(), Observer {

    private val startupActivity by lazy {
        requireActivity() as StartupActivity
    }
    val pid by lazy {
        view?.findViewById<EditText>(R.id.edit_filter_pid)
    }
    var running = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fratment_monitoring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterBuilder.pid = pid!!
        with(view) {
            findViewById<Button>(R.id.button_start_monitor).run {
                setOnClickListener {
                    if (running) {
                        text = "Run"
                        startupActivity.service?.run {
                            ElmCommTask.Monitor.stop()
                        }
                    } else {
                        val  command = filterBuilder.getCommand()
                        text = "Stop"
                        startupActivity.service?.run {
                            ElmCommTask.Monitor.start(this, command)
                        }
                    }
                    running = !running
                }

                text = "Run"
            }
            findViewById<RadioGroup>(R.id.group_choose_filter)?.run {
                setOnCheckedChangeListener(filterBuilder)
            }
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        Log.v(Const.TAG, "Monitoringragment::update")
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

        fun isValid(pid : String) : Boolean {
            return true //TODO
        }

        fun hexToByte(hexString : String) : Byte {
            if (hexString.length != 2) {
                return 0.toByte()
            }
            var value = 0
            hexString.toCharArray().forEach {
                value += value * 16 + Character.digit(it, 16)
            }
            return value.toByte()
        }
    }
}
package com.example.obdcontrol.ui

import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.task.ElmCommTask
import java.util.*

class MonitoringFragment : Fragment(), Observer {

    val startupActivity by lazy {
        requireActivity() as StartupActivity
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

        with(view) {
            findViewById<Button>(R.id.button_start_monitor).run {
                setOnClickListener {
                    if (running) {
                        text = "Stop"
                        monitor.stop()
                    } else {
                        text = "Run"
                        monitor.start(startupActivity.service)
                    }
                    running = !running
                }
            }
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        Log.v(Const.TAG, "Monitoringragment::update")
    }

    object monitor {

        var service: ElmCommTask? = null
        lateinit var logging : Spannable

        fun start(service : ElmCommTask?) {
            service?.run {
                this@monitor.service = this
                val command = filter.getCommand()
                send(command)
            }
        }

        fun stop() {
            service?.run {
                send(" ")
            }
            service = null
        }
    }

    object filter : RadioGroup.OnCheckedChangeListener {

        var sendPid : Byte = 0x40.toByte()
        var receievePid : Byte = 0xc0.toByte()

        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            TODO("Not yet implemented")
        }
        fun getCommand() : String {

            return "ATMA"
        }
    }
}
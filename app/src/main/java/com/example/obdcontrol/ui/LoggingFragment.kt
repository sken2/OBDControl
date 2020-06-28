package com.example.obdcontrol.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.obdcontrol.Const
import com.example.obdcontrol.Logging
import com.example.obdcontrol.R
import java.util.*

class LoggingFragment : Fragment() {

    val logging by lazy {
        view?.findViewById<TextView>(R.id.text_logging)?.apply {
            movementMethod = ScrollingMovementMethod()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(Const.TAG, "LoggingFragment::onCreateView")
        val rootView = inflater.inflate(R.layout.fragment_show_logging, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "LoggingFragment::onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.button_clear).apply {
            setOnClickListener {
                Toast.makeText(activity, "Comming Soon!!!", Toast.LENGTH_SHORT).show()  //TODO
            }
        }
        view.findViewById<ImageButton>(R.id.button_save).apply {
            setOnClickListener {
                Toast.makeText(activity, "Comming Soon!!!", Toast.LENGTH_SHORT).show()  //TODO
            }
        }
    }

    override fun onResume() {
        Log.v(Const.TAG, "LoggingFragment::onResume")
        super.onResume()

        Logging.addObserver(newMessageObserver)
        logging?.text = Logging.getMessage()
    }

    override fun onPause() {
        Log.v(Const.TAG, "LoggingFragment::onPause")
        Logging.deleteObserver(newMessageObserver)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }
        Log.v(Const.TAG, "SppChatFragment::onActivityResult request = $requestCode action = ${data.action} result = $resultCode")
        when(requestCode) {
            Const.Requests.REQUEST_SAVE -> {
                if (Activity.RESULT_OK == resultCode) {
                    data.data?.run {
                        activity?.contentResolver?.openOutputStream(this)?.let {
                            Logging.save(it)
                        }
                    }
                }
            }
        }
    }

    val newMessageObserver = object : Observer {
        override fun update(o: Observable?, arg: Any?) {
            activity?.runOnUiThread {
                logging?.text = Logging.getMessage()
            }
        }
    }
}
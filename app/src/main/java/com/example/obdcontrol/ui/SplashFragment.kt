package com.example.obdcontrol.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.obdcontrol.Const
import com.example.obdcontrol.R

class SplashFragment : Fragment() {

    val startupActivity by lazy {
        activity as StartupActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(Const.TAG, "SplashFragment::onCreateView")
        val rootView = inflater.inflate(R.layout.fragment_startup, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_connect)?.apply {
            setOnClickListener{
//                startupActivity.connect()
                Toast.makeText(context, "Connected(maybe)", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById<Button>(R.id.button_start_chat).apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_splashFragment_to_sppChatFragment)
            }
        }
        with(requireActivity()) {
            takeIf { this is StartupActivity }.run {
                //TODO
            }
        }
    }
}
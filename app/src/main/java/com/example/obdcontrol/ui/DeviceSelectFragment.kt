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
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.adapters.BtDevicesAdapter

class DeviceSelectFragment : Fragment() {

    private val startupActivity by lazy {
        activity as StartupActivity
    }
    private val adapter by lazy {
        BtDevicesAdapter()
    }
    private val preference by lazy {
        startupActivity.preference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(Const.TAG, "DeviceSearchFragment::onCreateView")
        val contentView = inflater.inflate(R.layout.fragment_select_device, container, false)
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "DeviceSearchFragment::onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<RecyclerView>(R.id.recycler_devices)?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = this@DeviceSelectFragment.adapter
        }
        view.findViewById<Button>(R.id.button_choose_it).apply {
            setOnClickListener {
                with (adapter.selectionTracker) {
                    if (hasSelection()) {
                        val device = adapter.getItemByKey(selection.first())
                        preference.apply {
                            edit().putString(Const.Preference.KEY_DEVICE, device.address).apply()
                        }
//                        startupActivity.setDevice(device)
                        findNavController().navigate(R.id.action_deviceSearchFragment_to_splashFragment)
                    }
                }
            }
        }
        view.findViewById<Button>(R.id.button_pair_now).apply {
            setOnClickListener {
                Toast.makeText(context, "Do pairling yourself", Toast.LENGTH_SHORT).show()  //TODO
            }
        }
    }

    val selectionObserver = object : SelectionTracker.SelectionObserver<String>() {

        override fun onItemStateChanged(key: String, selected: Boolean) {
            Log.v(Const.TAG, "DeviceSelectFragment::onItemStateChanged $key is $selected")
            super.onItemStateChanged(key, selected)
        }
    }
}
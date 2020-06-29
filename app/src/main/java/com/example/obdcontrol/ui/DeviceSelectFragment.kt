package com.example.obdcontrol.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.adapters.BtDevicesAdapter

class DeviceSelectFragment : Fragment() {

    private val adapter by lazy {
        BtDevicesAdapter()
    }
    private val preference by lazy {
        startupActivity.preference
    }
    private val startupActivity by lazy {
        activity as StartupActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(Const.TAG, "DeviceSearchFragment::onCreateView")
        val contentView = inflater.inflate(R.layout.fragment_select_device, container, false)
        contentView.findViewById<RecyclerView>(R.id.recycler_devices)?.apply {
            layoutManager = LinearLayoutManager(view?.context)
            adapter = this@DeviceSelectFragment.adapter
        }
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(Const.TAG, "DeviceSearchFragment::onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_choose_it).apply {
            setOnClickListener {
                val select = adapter.selected
                if (select != -1) {
                    val device = adapter.list.get(select)
                    if (activity is StartupActivity) {
                        val startupActivity = activity as StartupActivity
                        startupActivity.preference.apply {
                            edit().putString(Const.Preference.KEY_DEVICE, device.address).apply()
                        }
                        with(startupActivity) {
                            this.device = device
                            deviceName.text = getInformation()
                        }
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
}
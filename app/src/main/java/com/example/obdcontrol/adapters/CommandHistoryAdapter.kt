package com.example.obdcontrol.adapters

import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import org.json.JSONArray
import org.json.JSONObject

class CommandHistoryAdapter : RecyclerView.Adapter<CommandHistoryAdapter.ViewHolder>() {

    val history = mutableListOf<String>()
    lateinit var preference : SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.v(Const.TAG, "CommandHistoryAdapter::onCreateViewHolder")
        preference = PreferenceManager.getDefaultSharedPreferences(parent.context.applicationContext)
        val historyView = TextView(parent.context)
        return ViewHolder(historyView)
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            if (this is TextView) {
                this.text = history.get(position)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onAttachedToRecyclerViw")
        val commands = preference.getString(Const.Preference.KEY_COMMAND, "{}")!!
        val ja = JSONArray(commands)
        for (index in 0..ja.length()) {
            history.add(ja.get(index).toString())
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onDetachedFromRecyclerViw")
        val ja = JSONArray()
        history.forEach {
            val historyJson = JSONObject(it)
            ja.put(historyJson)
        }
        preference.edit().putString(Const.Preference.KEY_COMMAND, ja.toString()).apply()
    }

    fun issue(command : String) {
        synchronized(history) {
            history.remove(command) // replace old history to top
            history.add(command)
            while (history.size > 10) { // TODO look for more better way
                history.removeAt(0)
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

    }
}
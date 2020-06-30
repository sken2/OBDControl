package com.example.obdcontrol.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const

class CommandHistoryAdapter : RecyclerView.Adapter<CommandHistoryAdapter.ViewHolder>() {

    val history = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.v(Const.TAG, "CommandHistoryAdapter::onCreateViewHolder")
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
                return
            }
            throw Exception("oops")
        }
    }

    fun issue(command : String) {
        synchronized(history) {
            history.remove(command) // replace old history to top
            history.add(command)
            while (history.size > 10) { // TODO look for more better way
                history.removeAt(1)
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

    }
}
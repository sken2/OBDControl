package com.example.obdcontrol.adapters

import android.content.SharedPreferences
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import org.json.JSONArray
import org.json.JSONObject

class CommandHistoryAdapter : RecyclerView.Adapter<CommandHistoryAdapter.ViewHolder>() {

    val history = mutableListOf<String>()
    lateinit var preference : SharedPreferences
    lateinit var selectionTracker : SelectionTracker<String>
    lateinit var recyclerView : RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.v(Const.TAG, "CommandHistoryAdapter::onCreateViewHolder")

        val historyView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_command_text, parent, false)
        return ViewHolder(historyView)
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val commandText = holder.view.findViewById<TextView>(R.id.text_command_item)
        with(commandText) {
            val command = history.get(position)
//            holder.bind(command, selectionTracker.isSelected(command)) // have to do: where is bind ?
            this.text = command
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onAttachedToRecyclerViw")
        preference = PreferenceManager.getDefaultSharedPreferences(recyclerView.context.applicationContext)
        this.recyclerView = recyclerView
        val commands = preference.getString(Const.Preference.KEY_HISTORY, "[]")!!
        val ja = JSONArray(commands)
        if (ja.length() != 0) {
            for (index in 0..ja.length()-1) {
                history.add(ja.get(index).toString())
            }
        }
        // no setSelectionTrasker ?
        selectionTracker = SelectionTracker.Builder<String>(
            "selecction-id",
            recyclerView,
            HistoryKeyprovider(1, history),
            itemDatilLookup,
            StorageStrategy.createStringStorage()
        ).build().apply {
            addObserver(actionObserver)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onDetachedFromRecyclerView")
        if (history.isNotEmpty()) {
            val ja = JSONArray(history)
            preference.edit().putString(Const.Preference.KEY_HISTORY, ja.toString()).apply()
        }
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

    private var itemDatilLookup = object : ItemDetailsLookup<String>() {

        override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(e.getX(), e.getY())
            view?.run {
                val viewHolder = recyclerView.getChildViewHolder(view)
                if (viewHolder is CommandHistoryAdapter.ViewHolder) {
                    return viewHolder.getItemDetails()
                }
            }
            return null
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            Log.v(Const.TAG, "CommandHistoryAdapter::onActionItemClicked $mode $item")
            return false
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.v(Const.TAG, "CommandHistoryAdapter::onCreateActionMode $mode")
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.v(Const.TAG, "CommandHistoryAdapter::onPrepareActionMode $mode")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            Log.v(Const.TAG, "CommandHistoryAdapter::onDestroyActionMode $mode")
            selectionTracker.clearSelection()
        }
    }

    private object actionObserver : SelectionTracker.SelectionObserver<String>() {

        override fun onSelectionChanged() {
            super.onSelectionChanged()
        }
    }

    //    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view), ViewholderWithDetail { // where is ViewholderWithDetail ?
    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

        fun getItemDetails() : ItemDetails {
            return ItemDetails(absoluteAdapterPosition, history.get(absoluteAdapterPosition))
        }
    }

    inner class ItemDetails(private val adapterPosition : Int, private val selectionKey : String)
        : ItemDetailsLookup.ItemDetails<String>() {

        override fun getPosition(): Int {
            return adapterPosition
        }

        override fun getSelectionKey(): String? {
            return selectionKey
        }
    }
}
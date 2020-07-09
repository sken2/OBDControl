package com.example.obdcontrol.adapters

import android.content.SharedPreferences
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import com.example.obdcontrol.R
import com.example.obdcontrol.ui.SppChatFragment
import com.example.obdcontrol.ui.StartupActivity
import org.json.JSONArray

class CommandHistoryAdapter : RecyclerView.Adapter<CommandHistoryAdapter.ViewHolder>() {

    val history = mutableListOf<String>()
    lateinit var startupActivity: StartupActivity
    lateinit var sppChatFragment: SppChatFragment
    lateinit var preference : SharedPreferences
    lateinit var selectionTracker : SelectionTracker<String>
    lateinit var recyclerView : RecyclerView
    private var actionMode : ActionMode? = null

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
            this.isActivated = selectionTracker.isSelected(command)
            this.text = command
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onAttachedToRecyclerViw")
        preference = PreferenceManager.getDefaultSharedPreferences(recyclerView.context.applicationContext)
        this.startupActivity = recyclerView.context as StartupActivity
        this.sppChatFragment = FragmentManager.findFragment(recyclerView)
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
            addObserver(sppChatFragment.actionObserver)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Log.v(Const.TAG, "CommandHistoryAdapter::onDetachedFromRecyclerView")
        val ja = JSONArray(history)
        preference.edit().putString(Const.Preference.KEY_HISTORY, ja.toString()).apply()
        history.clear()
    }

    fun issue(command : String) {
        selectionTracker.clearSelection()
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
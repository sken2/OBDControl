package com.example.obdcontrol.entities

import android.content.Context
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.example.obdcontrol.Const
import com.example.obdcontrol.R

class SetupActionModeCallback(val appContext : Context) : ActionMode.Callback {

    val preference by lazy {
        PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        Log.v(Const.TAG, "SetupActionModeCallback::onActionItemClicked")
        var saveKey = 0
        if (item == null) return false
        when (item.itemId) {
            R.id.menu_setup_up -> saveKey = R.string.key_volume_up
            R.id.menu_setup_down -> saveKey = R.string.key_volume_down
            R.id.menu_setup_prev -> saveKey = R.string.key_prev_tune
            R.id.menu_setup_next -> saveKey = R.string.key_next_tune
            R.id.menu_setup_mute -> saveKey = R.string.key_mute
            else -> return false
        }
        mode?.customView.run {
            val selectedView = this as TextView
            val start = selectionStart
            val end = selectionEnd
            val selectedText = if (start > end) {
                text.subSequence(end, start)
            } else {
                text.subSequence(start, end)
            }
            preference.edit().putString(selectedText.toString(), appContext.getString(saveKey)).apply()
        }
        mode?.finish()
        return true
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        Log.v(Const.TAG, "SetupActionModeCallback::onCreateActionMode")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        Log.v(Const.TAG, "SetupActionModeCallback::onPrepareActionMode")
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        // nothing to do
    }
}
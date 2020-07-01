package com.example.obdcontrol.adapters

import androidx.recyclerview.selection.ItemKeyProvider

class HistoryKeyprovider (scope : Int, val itemList : List<String>): ItemKeyProvider<String>(scope) {
    override fun getKey(position: Int): String? {
        return itemList.get(position)
    }

    override fun getPosition(key: String): Int {
        return itemList.indexOf(key)
    }
}
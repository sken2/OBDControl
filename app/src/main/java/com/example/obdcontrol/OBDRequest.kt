package com.example.obdcontrol

import java.lang.Exception

open class OBDRequest (command : String) {
    private val command = command
    private var codes = emptyArray<Byte>()

    init {
        codes = Elm327.toBytes(command)
    }

    fun getPid() :Byte {
        return codes.first()
    }
    fun getOp() : Byte {
        if (codes.size >1) {
            return codes[1]
        }
        return -1
    }
}
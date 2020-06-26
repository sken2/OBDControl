package com.example.obdcontrol.obd2

object ResponseFactory {

    fun decode(message : String) : OBDResponse {
        return OBDResponse(byteArrayOf(-1))
    }
}
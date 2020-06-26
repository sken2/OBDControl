package com.example.obdcontrol.obd2

data class OBDResponse(var codes : ByteArray) {

    override fun toString() : String {
        var stringValue = ""
        codes.forEach { stringValue += "%02X ".format(it) }
        return stringValue.trim()
    }
}
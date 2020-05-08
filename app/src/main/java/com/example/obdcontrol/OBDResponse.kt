package com.example.obdcontrol

import kotlin.properties.Delegates

class OBDResponse(data : String, hasHeader : Boolean) {
    private var valid = false
    private var codes = emptyArray<Byte>()
    private val hasHeader = hasHeader

    init {
        var firstDigit = true
        var hexValue = 0
        data.forEach { chr ->
            val hex = Character.digit(chr, 16)
            if (hex != -1) {
                if (firstDigit) {
                    hexValue = hex *16
                } else {
                    codes += (hexValue + hex).toByte()
                    hexValue = 0
                }
                firstDigit = !firstDigit
            }   // TODO checkout non-hexadecimal char
        }
        if (codes.isNotEmpty() and firstDigit) {
            valid = true
        }
    }

    fun isValid() : Boolean {
        return valid
    }

    override fun toString() : String {
        var stringValue = ""
        codes.forEach { stringValue += "%02X ".format(it) }
        return stringValue.trim()
    }
}
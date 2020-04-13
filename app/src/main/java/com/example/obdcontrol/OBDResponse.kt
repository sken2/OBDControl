package com.example.obdcontrol

class OBDResponse(data : ByteArray) {
    var codes = emptyArray<Byte>()

    init {
        toCodes(data)
    }

    private fun toCodes(data : ByteArray) {
        var hexvalue = 0
        var firstByte = true
        data.forEach {
            when (Character.digit(it.toInt(), 16)) {
                -1 -> {}
                else -> {
                    if (firstByte) {
                        hexvalue = it * 16
                    } else {
                        codes += (hexvalue + it).toByte()
                    }
                    firstByte = !firstByte
                }
            }
        }
    }
}
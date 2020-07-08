package com.example.obdcontrol.obd2

object ResponseFactory {

    fun decode(message : String) : OBDResponse {
        var codes = emptyArray<Byte>()
        val byteList = message.split(" ")
        if (byteList.first().trim().length != 3) {
            return OBDResponse(byteArrayOf(-1))
        }
        for (code in byteList.subList(1, byteList.size)) {
            codes += toByte(code)
        }
        return OBDResponse(codes.toByteArray())
    }

    private fun toByte(hexString : String) : Byte {
        val chars = hexString.trim().toCharArray()
        if (chars.size != 2) {
            return 255.toByte()
        }
        val digith = Character.digit(chars[0], 16)
        val digitl = Character.digit(chars[1], 16)
        return (digith * 16 + digitl).toByte()
    }
}
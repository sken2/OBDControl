package com.example.obdcontrol

import com.example.obdcontrol.obd2.ResponseFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class OBDresponseTest {

    @Test
    fun singleCode() {
        val response = ResponseFactory.decode("3E1 00")
        assertEquals(response.codes[0], 0.toByte())
    }
    fun doubleCodes() {
        val response = ResponseFactory.decode(" 3F1 00  01")
        assertEquals(response.codes, byteArrayOf(0,1))
    }
}
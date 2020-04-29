package com.example.obdcontrol

class OBDResponse(data : String) : OBDRequest(data) {
    var isValid : Boolean = true
    private val org = data

    override fun toString() : String {
        return org  // TODO
    }
}
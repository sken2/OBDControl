package com.example.obdcontrol

object Const {

    val TAG = "OdbConnect"

    val CR = "\r"
    val LF = "\n"

    object Preference {
        const val PREFERENCE_NAME = "elmcontrol"
        const val KEY_DEVICE = "device"
        const val KEY_COMMAND = "commands"
        const val KEY_HISTORY = "history"
    }

    object Requests {
        val REQUEST_SELECT = 1234
        val REQUEST_SAVE = 2345
        val SAVE_REQUEST = 4567
    }

    object UUIDS {
        const val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    object Keys {
        const val Preset1 = "Preset1"
        const val Preset2 = "Preset2"
        const val Preset3 = "Preset3"
    }
}
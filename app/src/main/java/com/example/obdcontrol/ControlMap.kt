package com.example.obdcontrol.entities

object ControlMap {

    val pidMap = mutableMapOf<Control, Array<Byte>>()
    val controlMap = mutableMapOf<Control, String>()

    enum class Control(value : String) {
        VOLUME_UP("vUp"),
        VOLUME_DOWN("vDown"),
        NEXT_TUNE("nTune"),
        PREFIOUS_TUN("pTune"),
        NOTIHNG("nothing")
    }

    fun setPid(oparation : Control, action : Array<Byte>) {
        pidMap.put(oparation, action)
    }

    fun setControl(oparation: Control, action : String) {
        controlMap.put(oparation, action)
    }

    fun findControl(action : Array<Byte>) : Control {
        val actionMap = pidMap.filter { it.value.equals(action)}
        if (actionMap.isEmpty()) {
            return Control.NOTIHNG
        }
        return actionMap.keys.first()
    }

    fun getControl(control : Control) : String {
        return controlMap.getValue(control)
    }
}
package com.example.obdcontrol

import android.text.SpannableStringBuilder
import java.util.*

object Logging : Observable() {
    private var logging = arrayOf(Transaction("", Direction.RX))

    fun clear() {
        synchronized(logging) {
            this.logging = arrayOf(Transaction("", Direction.RX))
        }
    }

    fun send(message : String) {
        synchronized(logging) {
            logging += Transaction(message, Direction.TX)
        }
        setChanged()
        notifyObservers()
    }

    fun receive(message : String) {
        synchronized(logging) {
            when (logging.last().dicrection) {
                Direction.TX -> {
                    logging += Transaction(message, Direction.TX)
                }
                Direction.RX -> {
                    logging.last().text += message
                }
            }
        }
        setChanged()
        notifyObservers()
    }

    fun getMessage() : SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        synchronized(logging) {
            if (logging.size > 1100) {
                logging = logging.copyOfRange(100, logging.size)
            }
            logging.forEach { element -> builder.append(element.text) }
        }
        return builder
    }

    enum class Direction {
        TX, RX
    }

    data class Transaction(var text : String, val dicrection : Direction)
}
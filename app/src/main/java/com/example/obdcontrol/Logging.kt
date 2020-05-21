package com.example.obdcontrol

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import java.io.OutputStream
import java.lang.Exception
import java.util.*

object Logging : Observable() {
    private var logging = arrayOf(Transaction("", Direction.RX))

    fun clear() {
        synchronized(logging) {
            this.logging = arrayOf(Transaction("", Direction.RX))
            setChanged()
            notifyObservers()
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
                    logging += Transaction(message, Direction.RX)
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
            logging.forEach {
                if(it.text.isEmpty()) {
                    return@forEach
                }
                when (it.dicrection) {
                    Direction.RX -> builder.append(it.text, StyleSpan(Typeface.NORMAL), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    Direction.TX -> {
                        builder.append(it.text, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
        return builder
    }

    fun save(stream : OutputStream) : Boolean{
        try {
            stream.write(getMessage().toString().toByteArray())
        } catch (e : Exception) {
            return false
        } finally {
            stream.close()
        }
        return true
    }

    enum class Direction {
        TX, RX
    }

    data class Transaction(var text : String, val dicrection : Direction)
}
package com.example.stocker2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.stocker2.R

object ToastUtil {

    @SuppressLint("InflateParams")
    fun showCustomToast(context: Context, message: String) {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.custom_toast, null)

        val textView: TextView = layout.findViewById(R.id.custom_toast_message)
        textView.text = message

        with(Toast(context)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}

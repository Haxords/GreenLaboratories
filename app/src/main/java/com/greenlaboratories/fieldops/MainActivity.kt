package com.greenlaboratories.fieldops

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            text = "Green Laboratories Field Ops"
            textSize = 20f
        }
        setContentView(textView)
    }
}

fun ReportsScreen(viewModel: Any? = null) {
    // Empty placeholder
}

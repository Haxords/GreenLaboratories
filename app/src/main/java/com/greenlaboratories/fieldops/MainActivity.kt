package com.greenlaboratories.fieldops

import android.os.Bundle
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "Green Laboratories Field Ops\n\nApp Active & Working!"
            textSize = 22f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        
        setContentView(textView)
    }
}

fun ReportsScreen(viewModel: Any? = null) {
    // Left empty intentionally to prevent unresolved reference errors
}

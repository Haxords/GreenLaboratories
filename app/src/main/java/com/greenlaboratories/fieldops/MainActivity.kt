package com.greenlaboratories.fieldops

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSale = findViewById<Button>(R.id.btnSale)
        val btnPurchase = findViewById<Button>(R.id.btnPurchase)
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val btnHelp = findViewById<TextView>(R.id.btnHelp)

        btnSale?.setOnClickListener {
            Toast.makeText(this, "ক্যাশ বেচা অপশনে ক্লিক করা হয়েছে", Toast.LENGTH_SHORT).show()
        }

        btnPurchase?.setOnClickListener {
            Toast.makeText(this, "ক্যাশ কেনা অপশনে ক্লিক করা হয়েছে", Toast.LENGTH_SHORT).show()
        }

        btnExpense?.setOnClickListener {
            Toast.makeText(this, "খরচ অপশনে ক্লিক করা হয়েছে", Toast.LENGTH_SHORT).show()
        }

        btnHelp?.setOnClickListener {
            Toast.makeText(this, "হেল্প অপশনে ক্লিক করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }
}

fun ReportsScreen(viewModel: Any? = null) {
    // Empty placeholder
}

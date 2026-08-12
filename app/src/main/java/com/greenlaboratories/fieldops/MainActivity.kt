package com.greenlaboratories.fieldops

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

data class Party(
    var id: Int,
    var name: String,
    var phone: String,
    var address: String,
    var dueAmount: Double = 0.0
)

class MainActivity : AppCompatActivity() {

    private val partyList = mutableListOf<Party>()
    private var partyIdCounter = 1
    private var todaySales = 0.0
    private var currentCash = 20916.0

    private lateinit var tvTodaySales: TextView
    private lateinit var tvCurrentCash: TextView
    private lateinit var partyContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTodaySales = findViewById(R.id.tvTodaySales)
        tvCurrentCash = findViewById(R.id.tvCurrentCash)
        partyContainer = findViewById(R.id.partyContainer)

        val btnAddParty = findViewById<Button>(R.id.btnAddParty)
        val btnCollection = findViewById<Button>(R.id.btnCollection)
        val btnHelp = findViewById<TextView>(R.id.btnHelp)

        // SharedPreferences থেকে পূর্বে সংরক্ষিত ডাটা লোড করা
        loadDataFromLocal()

        btnAddParty?.setOnClickListener { showAddPartyDialog() }
        btnCollection?.setOnClickListener { showCollectionDialog() }
        btnHelp?.setOnClickListener {
            Toast.makeText(this, "গ্রীন ল্যাবরেটরিজ হেল্পলাইন: 01306373232", Toast.LENGTH_LONG).show()
        }

        updateCashUI()
    }

    // --- SharedPreferences Data Save & Load Logic ---

    private fun saveDataToLocal() {
        val sharedPreferences = getSharedPreferences("FieldOpsData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonArray = JSONArray()
        for (party in partyList) {
            val jsonObject = JSONObject().apply {
                put("id", party.id)
                put("name", party.name)
                put("phone", party.phone)
                put("address", party.address)
                put("dueAmount", party.dueAmount)
            }
            jsonArray.put(jsonObject)
        }

        editor.putString("party_list", jsonArray.toString())
        editor.putFloat("today_sales", todaySales.toFloat())
        editor.putFloat("current_cash", currentCash.toFloat())
        editor.putInt("party_id_counter", partyIdCounter)
        editor.apply()
    }

    private fun loadDataFromLocal() {
        val sharedPreferences = getSharedPreferences("FieldOpsData", Context.MODE_PRIVATE)
        val partyDataStr = sharedPreferences.getString("party_list", null)

        todaySales = sharedPreferences.getFloat("today_sales", 0.0f).toDouble()
        currentCash = sharedPreferences.getFloat("current_cash", 20916.0f).toDouble()
        partyIdCounter = sharedPreferences.getInt("party_id_counter", 1)

        partyList.clear()
        if (!partyDataStr.isNullOrEmpty()) {
            val jsonArray = JSONArray(partyDataStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                partyList.add(
                    Party(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        address = obj.getString("address"),
                        dueAmount = obj.getDouble("dueAmount")
                    )
                )
            }
        } else {
            // প্রথমবার ইনস্টল করলে ডিফল্ট ডাটা
            partyList.add(Party(partyIdCounter++, )
            saveDataToLocal()
        }
        renderPartyList()
    }

    private fun updateCashUI() {
        tvTodaySales.text = "৳ ${todaySales.toInt()}"
        tvCurrentCash.text = "৳ ${currentCash.toInt()}"
    }

    private fun renderPartyList() {
        partyContainer.removeAllViews()

        for (party in partyList) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(24, 24, 24, 24)
                elevation = 4f
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                layoutParams = params
            }

            val tvTitle = TextView(this).apply {
                text = party.name
                textSize = 18f
                setTextColor(Color.BLACK)
            }

            val tvDetails = TextView(this).apply {
                text = "ফোন: ${party.phone} | ঠিকানা: ${party.address}\nবাকি প্রডাক্ট হিসাব: ৳ ${party.dueAmount.toInt()}"
                textSize = 14f
                setTextColor(Color.GRAY)
            }

            val btnLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val btnEdit = Button(this).apply {
                text = "এডিট"
                setOnClickListener { showEditPartyDialog(party) }
            }

            val btnDelete = Button(this).apply {
                text = "মুছুন"
                setTextColor(Color.RED)
                setOnClickListener {
                    partyList.remove(party)
                    saveDataToLocal()
                    renderPartyList()
                    Toast.makeText(this@MainActivity, "পার্টি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }

            val btnAddProduct = Button(this).apply {
                text = "+ প্রডাক্ট দিন"
                setOnClickListener { showAddProductDialog(party) }
            }

            btnLayout.addView(btnAddProduct)
            btnLayout.addView(btnEdit)
            btnLayout.addView(btnDelete)

            card.addView(tvTitle)
            card.addView(tvDetails)
            card.addView(btnLayout)

            partyContainer.addView(card)
        }
    }

    private fun showAddPartyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("নতুন পার্টি যোগ করুন")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val etName = EditText(this).apply { hint = "পার্টির নাম" }
        val etPhone = EditText(this).apply { hint = "ফোন নম্বর"; inputType = InputType.TYPE_CLASS_PHONE }
        val etAddress = EditText(this).apply { hint = "ঠিকানা" }

        layout.addView(etName)
        layout.addView(etPhone)
        layout.addView(etAddress)
        builder.setView(layout)

        builder.setPositiveButton("সেভ করুন") { _, _ ->
            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val address = etAddress.text.toString()

            if (name.isNotEmpty()) {
                partyList.add(Party(partyIdCounter++, name, phone, address))
                saveDataToLocal()
                renderPartyList()
                Toast.makeText(this, "নতুন পার্টি যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }

    private fun showEditPartyDialog(party: Party) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("পার্টি এডিট করুন")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val etName = EditText(this).apply { setText(party.name) }
        val etPhone = EditText(this).apply { setText(party.phone); inputType = InputType.TYPE_CLASS_PHONE }
        val etAddress = EditText(this).apply { setText(party.address) }

        layout.addView(etName)
        layout.addView(etPhone)
        layout.addView(etAddress)
        builder.setView(layout)

        builder.setPositiveButton("আপডেট") { _, _ ->
            party.name = etName.text.toString()
            party.phone = etPhone.text.toString()
            party.address = etAddress.text.toString()
            saveDataToLocal()
            renderPartyList()
            Toast.makeText(this, "তথ্য আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }

    private fun showAddProductDialog(party: Party) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("${party.name}-কে প্রডাক্ট দিন")

        val etAmount = EditText(this).apply {
            hint = "প্রডাক্ট এর মোট টাকা"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        builder.setView(etAmount)

        builder.setPositiveButton("এন্ট্রি দিন") { _, _ ->
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                party.dueAmount += amount
                saveDataToLocal()
                renderPartyList()
                Toast.makeText(this, "প্রডাক্ট বাকি হিসেবে যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }

    private fun showCollectionDialog() {
        if (partyList.isEmpty()) {
            Toast.makeText(this, "কোনো পার্টি নেই!", Toast.LENGTH_SHORT).show()
            return
        }

        val partyNames = partyList.map { "${it.name} (বাকি: ৳${it.dueAmount.toInt()})" }.toTypedArray()
        var selectedPartyIndex = 0

        val builder = AlertDialog.Builder(this)
        builder.setTitle("টাকা কালেকশন করুন")

        builder.setSingleChoiceItems(partyNames, 0) { _, which -> selectedPartyIndex = which }

        val etAmount = EditText(this).apply {
            hint = "কালেকশন এর পরিমাণ (টাকা)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
            addView(etAmount)
        }
        builder.setView(layout)

        builder.setPositiveButton("কালেকশন করুন") { _, _ ->
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val selectedParty = partyList[selectedPartyIndex]
                selectedParty.dueAmount -= amount
                if (selectedParty.dueAmount < 0) selectedParty.dueAmount = 0.0

                todaySales += amount
                currentCash += amount

                saveDataToLocal()
                updateCashUI()
                renderPartyList()

                Toast.makeText(this, "৳$amount কালেকশন সফল হয়েছে!", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }
}

fun ReportsScreen(viewModel: Any? = null) {}

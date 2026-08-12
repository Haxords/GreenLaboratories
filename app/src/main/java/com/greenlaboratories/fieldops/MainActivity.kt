package com.greenlaboratories.fieldops

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
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

data class Product(
    var id: Int,
    var name: String,
    var unitPrice: Double
)

class MainActivity : AppCompatActivity() {

    private val partyList = mutableListOf<Party>()
    private val productList = mutableListOf<Product>()
    private var partyIdCounter = 1
    private var productIdCounter = 1
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
            showOrderSheetDialog()
        }

        updateCashUI()
    }

    // --- SharedPreferences Data Save & Load ---

    private fun saveDataToLocal() {
        val sharedPreferences = getSharedPreferences("FieldOpsData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save Parties
        val partyArray = JSONArray()
        for (party in partyList) {
            val obj = JSONObject().apply {
                put("id", party.id)
                put("name", party.name)
                put("phone", party.phone)
                put("address", party.address)
                put("dueAmount", party.dueAmount)
            }
            partyArray.put(obj)
        }

        // Save Products
        val productArray = JSONArray()
        for (product in productList) {
            val obj = JSONObject().apply {
                put("id", product.id)
                put("name", product.name)
                put("unitPrice", product.unitPrice)
            }
            productArray.put(obj)
        }

        editor.putString("party_list", partyArray.toString())
        editor.putString("product_list", productArray.toString())
        editor.putFloat("today_sales", todaySales.toFloat())
        editor.putFloat("current_cash", currentCash.toFloat())
        editor.putInt("party_id_counter", partyIdCounter)
        editor.putInt("product_id_counter", productIdCounter)
        editor.apply()
    }

    private fun loadDataFromLocal() {
        val sharedPreferences = getSharedPreferences("FieldOpsData", Context.MODE_PRIVATE)
        val partyDataStr = sharedPreferences.getString("party_list", null)
        val productDataStr = sharedPreferences.getString("product_list", null)

        todaySales = sharedPreferences.getFloat("today_sales", 0.0f).toDouble()
        currentCash = sharedPreferences.getFloat("current_cash", 20916.0f).toDouble()
        partyIdCounter = sharedPreferences.getInt("party_id_counter", 1)
        productIdCounter = sharedPreferences.getInt("product_id_counter", 1)

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
            partyList.add(Party(partyIdCounter++, "ডাক্তার কাজল রায় (Boro vita)", "01700000000", "রংপুর", 62600.0))
        }

        productList.clear()
        if (!productDataStr.isNullOrEmpty()) {
            val jsonArray = JSONArray(productDataStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                productList.add(
                    Product(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        unitPrice = obj.getDouble("unitPrice")
                    )
                )
            }
        } else {
            // ডিফল্ট প্রডাক্ট লিস্ট
            productList.add(Product(productIdCounter++, "Artasin Tab", 150.0))
            productList.add(Product(productIdCounter++, "Dietin Cap", 220.0))
            productList.add(Product(productIdCounter++, "Borovita Syrup", 180.0))
        }

        saveDataToLocal()
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

    // --- Order Sheet & Automatic Total Price Calculation Dialog ---

    private fun showOrderSheetDialog() {
        if (partyList.isEmpty()) {
            Toast.makeText(this, "আগে একটি পার্টি তৈরি করুন!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_order_sheet, null)
        val spParties = dialogView.findViewById<Spinner>(R.id.spParties)
        val spProducts = dialogView.findViewById<Spinner>(R.id.spProducts)
        val tvUnitPrice = dialogView.findViewById<TextView>(R.id.tvUnitPrice)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val tvTotalPrice = dialogView.findViewById<TextView>(R.id.tvTotalPrice)
        val btnManageProducts = dialogView.findViewById<Button>(R.id.btnManageProducts)

        // Spinners Populate
        val partyNames = partyList.map { it.name }
        spParties.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, partyNames)

        val productNames = productList.map { "${it.name} (৳${it.unitPrice.toInt()})" }
        spProducts.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, productNames)

        var selectedProduct = if (productList.isNotEmpty()) productList[0] else null
        var calculatedTotal = 0.0

        spProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (productList.isNotEmpty()) {
                    selectedProduct = productList[position]
                    tvUnitPrice.text = "একক দাম: ৳ ${selectedProduct?.unitPrice?.toInt()}"
                    
                    // Recalculate
                    val qty = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                    calculatedTotal = (selectedProduct?.unitPrice ?: 0.0) * qty
                    tvTotalPrice.text = "মোট দাম: ৳ ${calculatedTotal.toInt()}"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Auto Calculation when quantity changes
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val qty = s.toString().toDoubleOrNull() ?: 0.0
                calculatedTotal = (selectedProduct?.unitPrice ?: 0.0) * qty
                tvTotalPrice.text = "মোট দাম: ৳ ${calculatedTotal.toInt()}"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        btnManageProducts.setOnClickListener {
            dialog.dismiss()
            showManageProductsDialog()
        }

        builder.setPositiveButton("অর্ডার কনফার্ম করুন") { _, _ ->
            val selectedPartyIndex = spParties.selectedItemPosition
            if (selectedPartyIndex in partyList.indices && calculatedTotal > 0) {
                val party = partyList[selectedPartyIndex]
                party.dueAmount += calculatedTotal
                saveDataToLocal()
                renderPartyList()
                Toast.makeText(this, "অর্ডার সফল! ৳${calculatedTotal.toInt()} বাকি হিসেবে যোগ হলো", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }

    // --- Manage & Edit Products ---

    private fun showManageProductsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("প্রডাক্ট এডিট ও নতুন যোগ করুন")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val btnAddNew = Button(this).apply {
            text = "+ নতুন প্রডাক্ট যোগ করুন"
            setOnClickListener { showAddOrEditProductDialog(null) }
        }
        layout.addView(btnAddNew)

        for (prod in productList) {
            val prodLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvInfo = TextView(this).apply {
                text = "${prod.name} - ৳${prod.unitPrice.toInt()}"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 16f
            }

            val btnEditProd = Button(this).apply {
                text = "এডিট"
                setOnClickListener { showAddOrEditProductDialog(prod) }
            }

            prodLayout.addView(tvInfo)
            prodLayout.addView(btnEditProd)
            layout.addView(prodLayout)
        }

        builder.setView(layout)
        builder.setPositiveButton("বন্ধ করুন", null)
        builder.show()
    }

    private fun showAddOrEditProductDialog(product: Product?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (product == null) "নতুন প্রডাক্ট যোগ" else "প্রডাক্ট এডিট")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val etName = EditText(this).apply {
            hint = "প্রডাক্টের নাম"
            product?.let { setText(it.name) }
        }

        val etUnitPrice = EditText(this).apply {
            hint = "একক দাম (Unit Price)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            product?.let { setText(it.unitPrice.toString()) }
        }

        layout.addView(etName)
        layout.addView(etUnitPrice)
        builder.setView(layout)

        builder.setPositiveButton("সেভ করুন") { _, _ ->
            val name = etName.text.toString()
            val price = etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && price > 0) {
                if (product == null) {
                    productList.add(Product(productIdCounter++, name, price))
                } else {
                    product.name = name
                    product.unitPrice = price
                }
                saveDataToLocal()
                Toast.makeText(this, "প্রডাক্ট আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("বাতিল", null)
        builder.show()
    }

    // --- Standard Party Actions ---

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
        showOrderSheetDialog()
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

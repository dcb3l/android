package com.example.lr1v

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
	private lateinit var cbHawaii: CheckBox
	private lateinit var cbBBQ: CheckBox
	private lateinit var cbVeggie: CheckBox

	private lateinit var cbS: CheckBox
	private lateinit var cbM: CheckBox
	private lateinit var cbL: CheckBox

	private lateinit var cbOnion: CheckBox
	private lateinit var cbTomato: CheckBox
	private lateinit var cbCorn: CheckBox
	private lateinit var cbChicken: CheckBox

    private lateinit var btnOk: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)

		cbHawaii = findViewById(R.id.cbHawaii)
		cbBBQ = findViewById(R.id.cbBBQ)
		cbVeggie = findViewById(R.id.cbVeggie)

		cbS = findViewById(R.id.cbS)
		cbM = findViewById(R.id.cbM)
		cbL = findViewById(R.id.cbL)

		cbOnion = findViewById(R.id.cbOnion)
		cbTomato = findViewById(R.id.cbTomato)
		cbCorn = findViewById(R.id.cbCorn)
		cbChicken = findViewById(R.id.cbChicken)

        btnOk = findViewById(R.id.btnOk)
        tvResult = findViewById(R.id.tvResult)

        btnOk.setOnClickListener {
            showOrder()
        }
    }

    private fun showOrder() {
        val name = etName.text.toString().trim()

        val pizzaTypes = mutableListOf<String>()
        if (cbHawaii.isChecked) pizzaTypes.add("Гавайська")
        if (cbBBQ.isChecked) pizzaTypes.add("BBQ")
        if (cbVeggie.isChecked) pizzaTypes.add("Овочева")

        val sizes = mutableListOf<String>()
        if (cbS.isChecked) sizes.add("S")
        if (cbM.isChecked) sizes.add("M")
        if (cbL.isChecked) sizes.add("L")

        val extras = mutableListOf<String>()
        if (cbOnion.isChecked) extras.add("Цибуля")
        if (cbTomato.isChecked) extras.add("Томати")
        if (cbCorn.isChecked) extras.add("Кукурудза")
        if (cbChicken.isChecked) extras.add("Курка")

        if (name.isEmpty() || pizzaTypes.isEmpty() || sizes.isEmpty()) {
            Toast.makeText(
                this,
                "Будь ласка, заповніть усі обов'язкові дані!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val extrasText = if (extras.isEmpty()) {
            "без додаткових інгредієнтів"
        } else {
            extras.joinToString(", ")
        }

        val resultText = """
            Клієнт: $name

            Тип піци: ${pizzaTypes.joinToString(", ")}
            Розмір: ${sizes.joinToString(", ")}
            Додаткові інгредієнти: $extrasText
        """.trimIndent()

        tvResult.text = resultText
    }
}
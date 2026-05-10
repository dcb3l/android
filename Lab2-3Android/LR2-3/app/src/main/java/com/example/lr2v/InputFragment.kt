package com.example.lr2v

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class InputFragment : Fragment() {

    private var listener: OnOrderActionListener? = null

    private lateinit var etName: EditText
    private lateinit var cbMargarita: CheckBox
    private lateinit var cbPepperoni: CheckBox
    private lateinit var cbFourCheese: CheckBox

    private lateinit var cbSmall: CheckBox
    private lateinit var cbMedium: CheckBox
    private lateinit var cbLarge: CheckBox

    private lateinit var cbMushrooms: CheckBox
    private lateinit var cbOlives: CheckBox
    private lateinit var cbExtraCheese: CheckBox
    private lateinit var cbBacon: CheckBox

    private lateinit var btnOk: Button
    private lateinit var btnOpen: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnOrderActionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnOrderActionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_input, container, false)

        etName = view.findViewById(R.id.etName)

        cbMargarita = view.findViewById(R.id.cbMargarita)
        cbPepperoni = view.findViewById(R.id.cbPepperoni)
        cbFourCheese = view.findViewById(R.id.cbFourCheese)

        cbSmall = view.findViewById(R.id.cbSmall)
        cbMedium = view.findViewById(R.id.cbMedium)
        cbLarge = view.findViewById(R.id.cbLarge)

        cbMushrooms = view.findViewById(R.id.cbMushrooms)
        cbOlives = view.findViewById(R.id.cbOlives)
        cbExtraCheese = view.findViewById(R.id.cbExtraCheese)
        cbBacon = view.findViewById(R.id.cbBacon)

        btnOk = view.findViewById(R.id.btnOk)
        btnOpen = view.findViewById(R.id.btnOpen)

        btnOk.setOnClickListener {
            showOrder()
        }

        btnOpen.setOnClickListener {
            listener?.onOpenStorageRequested()
        }

        return view
    }

    private fun showOrder() {
        val name = etName.text.toString().trim()

        val pizzaTypes = mutableListOf<String>()
        if (cbMargarita.isChecked) pizzaTypes.add("Маргарита")
        if (cbPepperoni.isChecked) pizzaTypes.add("Пепероні")
        if (cbFourCheese.isChecked) pizzaTypes.add("Чотири сири")

        val sizes = mutableListOf<String>()
        if (cbSmall.isChecked) sizes.add("мала")
        if (cbMedium.isChecked) sizes.add("середня")
        if (cbLarge.isChecked) sizes.add("велика")

        val extras = mutableListOf<String>()
        if (cbMushrooms.isChecked) extras.add("гриби")
        if (cbOlives.isChecked) extras.add("оливки")
        if (cbExtraCheese.isChecked) extras.add("додатковий сир")
        if (cbBacon.isChecked) extras.add("бекон")

        if (name.isEmpty() || pizzaTypes.isEmpty() || sizes.isEmpty()) {
            Toast.makeText(
                requireContext(),
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

        listener?.onOrderSubmitted(resultText)
    }

    interface OnOrderActionListener {
        fun onOrderSubmitted(resultText: String)
        fun onOpenStorageRequested()
    }
}
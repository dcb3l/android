package com.example.lr2v

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StorageActivity : AppCompatActivity() {

    private lateinit var etStorageData: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        etStorageData = findViewById(R.id.etStorageData)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnRefresh = findViewById(R.id.btnRefresh)

        loadData()

        btnUpdate.setOnClickListener {
            updateData()
        }

        btnDelete.setOnClickListener {
            deleteData()
        }

        btnRefresh.setOnClickListener {
            loadData()
            Toast.makeText(this, "Дані оновлено на екрані", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        val data = StorageHelper.readOrders(this)

        if (data.isBlank()) {
            etStorageData.setText("")
            etStorageData.hint = "Сховище пусте. Дані відсутні."
        } else {
            etStorageData.setText(data)
        }
    }

    private fun updateData() {
        val newText = etStorageData.text.toString()

        val isUpdated = StorageHelper.updateOrders(this, newText)

        if (isUpdated) {
            Toast.makeText(this, "Дані у сховищі оновлено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Помилка оновлення даних", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteData() {
        val isDeleted = StorageHelper.deleteOrders(this)

        if (isDeleted) {
            etStorageData.setText("")
            etStorageData.hint = "Сховище пусте. Дані відсутні."
            Toast.makeText(this, "Дані зі сховища видалено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Помилка видалення даних", Toast.LENGTH_SHORT).show()
        }
    }
}
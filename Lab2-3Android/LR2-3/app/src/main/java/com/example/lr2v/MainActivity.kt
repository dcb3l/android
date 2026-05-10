package com.example.lr2v

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(),
    InputFragment.OnOrderActionListener,
    ResultFragment.OnCancelListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, InputFragment())
                .commit()
        }
    }

    override fun onOrderSubmitted(resultText: String) {
        val isSaved = StorageHelper.saveOrder(this, resultText)

        if (isSaved) {
            Toast.makeText(this, "Дані успішно збережено у файл", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Помилка під час збереження даних", Toast.LENGTH_SHORT).show()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ResultFragment.newInstance(resultText))
            .commit()
    }

    override fun onOpenStorageRequested() {
        val intent = Intent(this, StorageActivity::class.java)
        startActivity(intent)
    }

    override fun onCancelPressed() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, InputFragment())
            .commit()
    }
}
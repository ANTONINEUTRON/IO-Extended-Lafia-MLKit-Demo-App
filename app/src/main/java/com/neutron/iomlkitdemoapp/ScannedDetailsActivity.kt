package com.neutron.iomlkitdemoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ScannedDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_details)

        val editText: EditText = findViewById(R.id.text)

        val textEntered = intent.getStringExtra("TEXT")

        textEntered?.let{
            editText.setText(textEntered)
        }

        findViewById<Button>(R.id.leave).setOnClickListener{
            finishAffinity()
        }

        findViewById<Button>(R.id.scan_again).setOnClickListener {
            finish()
        }
    }
}
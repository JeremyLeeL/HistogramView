package com.jeremy.lym.histogramview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val strArray = arrayListOf("Froyofffffffff", "GB", "ICS", "JB", "KitKat", "L", "M")
        val countArray = arrayListOf(1f, 1.7f, 1.6f, 16.7f, 29.2f, 35.5f, 15.2345f)
        histogramView.setXAxisStrings(strArray)
        histogramView.setPillarsNumbers(countArray)
    }
}

package com.example.testcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.testcalendar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar)
        handleClicks(binding)
    }

    private fun handleClicks(binding: ActivityMainBinding) {
        binding.calendarViewSample.setOnClickListener {
            startActivity(Intent(this, CalendarViewActivity::class.java))
        }
//        binding.calendarComposeSample.setOnClickListener {
//            startActivity(Intent(this, CalendarComposeActivity::class.java))
//        }
    }
}
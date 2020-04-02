package com.jesusmar.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jesusmar.happyplaces.R
import com.jesusmar.happyplaces.database.DatabaseHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddHappyPlace.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        val handler = DatabaseHandler(this)
        val happyPlaceList = handler.getHappyPlaceList()

        for (a in happyPlaceList){
            println("title  ${a.title}")
        }

    }
}

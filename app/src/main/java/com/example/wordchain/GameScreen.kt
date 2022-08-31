package com.example.wordchain

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wordchain.databinding.ActivityGameScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class GameScreen : AppCompatActivity() {

    val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        fillData()
    }

    private fun fillData() {
        val random: Int = Random().nextInt(4)
        val ref = database.getReference("Puzzles").child(random.toString())
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val puzzle = snapshot.getValue() as ArrayList<*>

                var row = 0
                for (pz in puzzle)
                {
                    pz as String
                    for(col in 0 until pz.length)
                    {
                        val resID = resources.getIdentifier(
                            "tv_grid_$row$col",
                            "id", packageName
                        )
                        val tv = findViewById<View>(resID) as TextView
                        tv.setText(pz[col].toString())
                    }
                    row++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameScreen, error.toString(), Toast.LENGTH_SHORT).show()
            }

        })
    }
}
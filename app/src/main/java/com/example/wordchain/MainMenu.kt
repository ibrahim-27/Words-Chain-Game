package com.example.wordchain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wordchain.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {

    lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        binding.btnPlay.setOnClickListener()
        {
            startActivity(Intent(this, GameScreen::class.java))
        }

        /** Exit from the Game **/
        binding.btnQuit.setOnClickListener()
        {
            finishAffinity()
            System.exit(0)
        }
    }
}
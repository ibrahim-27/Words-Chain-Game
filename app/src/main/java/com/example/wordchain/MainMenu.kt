package com.example.wordchain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.wordchain.databinding.ActivityMainMenuBinding
import com.google.firebase.auth.FirebaseAuth

class MainMenu : AppCompatActivity() {

    lateinit var binding: ActivityMainMenuBinding
    var  auth=FirebaseAuth.getInstance()
    var user=auth.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnPlayOnline.setOnClickListener(){
            if(user!=null){
                val intent=Intent(this,OnlineHome::class.java)
                startActivity(intent)
            }
            else{
//                login
                val intent=Intent(this,SignInActivity::class.java)
                startActivity(intent)
            }
        }
        binding.btnPlayOffline.setOnClickListener()
        {
            startActivity(Intent(this, OfflineGameScreen::class.java))
        }

        /** Exit from the Game **/
        binding.btnQuit.setOnClickListener()
        {
            finishAffinity()
            System.exit(0)
        }
    }
}
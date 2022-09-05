package com.example.wordchain

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.wordchain.Models.Puzzle
import com.example.wordchain.databinding.ActivityGameScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class OfflineGameScreen : AppCompatActivity() {

    /** Firebase references **/
    val database = Firebase.database

    /** Game Objects **/
    lateinit var puzzle:Puzzle
    var currWord = 1
    var currChar = 0

    /** UI Components **/
    lateinit var soundEffect:MediaPlayer
    lateinit var animView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        animView = binding.animSuccess

        /** Game Setup **/
        GameSetup()

        /** Whem guess btn is pressed **/
        binding.btnGuess.setOnClickListener()
        {
            /** Players guesses the word correctly **/
            if(puzzle.wordsList[currWord].toString() == binding.etGuess.text.toString())
            {
                CompleteWord()
                SuccessAnimation()
            }
            else
            {
                ShowOneChar()
                WrongAnimation()
            }
        }
    }

    /** Game setup - fetch a puzzle from database and set the first and last word **/
    private fun GameSetup() {

        animView.pauseAnimation()
        //soundEffect.pause()

        val random: Int = Random().nextInt(4)
        val ref = database.getReference("Puzzles").child(random.toString())
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                puzzle = Puzzle(snapshot.value as ArrayList<*>)

                var row = 0
                for (pz in puzzle.wordsList)
                {
                    if(row == 0 || row == 4)
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
                    }
                    row++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OfflineGameScreen, error.toString(), Toast.LENGTH_SHORT).show()
            }

        })
    }

    /** If the user enters a wrong guess - show a new single character **/
    private fun ShowOneChar() {
        val guessWord = puzzle.wordsList[currWord].toString()

        if(guessWord.length-1 == currChar)
        {
            Toast.makeText(this, "no beta", Toast.LENGTH_SHORT).show()
            return
        }


        val resID = resources.getIdentifier(
            "tv_grid_${currWord}${currChar}",
            "id", packageName
        )
        val tv = findViewById<View>(resID) as TextView
        tv.setText(guessWord[currChar].toString())

        currChar++  // next char
    }

    /** If the player guesses the word - complete word is displayed **/
    private fun CompleteWord() {
        val guessWord = puzzle.wordsList[currWord].toString()

        for(i in currChar until guessWord.length)
        {
            val resID = resources.getIdentifier(
                "tv_grid_${currWord}${i}",
                "id", packageName
            )
            val tv = findViewById<View>(resID) as TextView
            tv.setText(guessWord[i].toString())
        }

        currWord++  // move to next word
        currChar = 0    // from the start of the word
        
        if(currWord == 4)
            startActivity(Intent(this, MainMenu::class.java))

    }

    /** Animations according to the answer **/
    private fun WrongAnimation() {
        animView.setAnimation(R.raw.animation_wrong)
        animView.playAnimation()
        soundEffect = MediaPlayer.create(this, R.raw.sound_wrong)
        soundEffect.start()
    }

    private fun SuccessAnimation() {
        animView.setAnimation(R.raw.animation_success)
        animView.playAnimation()
        soundEffect = MediaPlayer.create(this, R.raw.sound_success)
        soundEffect.start()
    }
}
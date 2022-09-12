package com.example.wordchain

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.wordchain.Models.Puzzle
import com.example.wordchain.Models.Room
import com.example.wordchain.databinding.ActivityOnlineGameScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class OnlineGameScreen : AppCompatActivity() {
	lateinit var binding: ActivityOnlineGameScreenBinding

	//variable for game
	lateinit var puzzle: Puzzle
	var turn = 0
	var player = 0
	var roomId = ""
	var currWord = 1
	var currChar = -1

	lateinit var room: Room

	/** UI Components **/
	lateinit var soundEffect: MediaPlayer
	lateinit var animView: LottieAnimationView

	//Firebase
	private var database = FirebaseDatabase.getInstance()
	private var auth = FirebaseAuth.getInstance().currentUser


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityOnlineGameScreenBinding.inflate(layoutInflater)
		setContentView(binding.root)
		supportActionBar?.hide()

		if (auth == null) {
			val intent = Intent(this, MainMenu::class.java)
			startActivity(intent)
		}
		animView = binding.animSuccesso


		//After entering room
		val extras = intent.extras
		if (extras != null) {
			roomId = extras.getString("key")!!

			var ref = database.getReference("ROOM").child(roomId)
			ref.addValueEventListener(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					room = Room(snapshot.child("id").value as String,
					            snapshot.child("player1_id").value as String,
					            snapshot.child("player2_id").value as String,
					            snapshot.child("turn").value as Long,
					            snapshot.child("puzzle").value as ArrayList<String>,
					            snapshot.child("wordNo").value as Long,
					            snapshot.child("charNo").value as Long)

					if (this@OnlineGameScreen::room.isInitialized && player == 0) {
						/** Game Setup **/
						GameSetup()

						if (auth?.uid == room.Player1_id) {
							player = 1
						}
						else if (auth?.uid == room.Player2_id) {
							player = 2
						}

						Toast.makeText(this@OnlineGameScreen, player.toString(), Toast.LENGTH_LONG)
							.show()
					}
					if (this@OnlineGameScreen::room.isInitialized && room.Player2_id != "") {
						startGame()
					}
				}

				override fun onCancelled(error: DatabaseError) {}

			})
		}
		/** Whem guess btn is pressed **/
		binding.btnGuesso.setOnClickListener() {
			/** Players guesses the word correctly **/
			if (puzzle.wordsList[currWord].toString() == binding.etGuesso.text.toString()) {
				CompleteWord()
				SuccessAnimation()
			}
			else {
				ShowOneChar()
				WrongAnimation()
			}			//changing turn
			if (turn == 1) {
				turn = 0
			}
			else {
				turn = 1
			}
			database.getReference("ROOM").child(roomId).child("turn").setValue(turn)
		}



		/** Three on data change are used to access database when these things changes and update UI of both devices **/

		/* For Completing Full Word*/
		var ref = database.getReference("ROOM").child(roomId).child("wordNo")
		ref.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				if (snapshot.value.toString().toInt() != 0 && snapshot.value.toString().toInt() != 4) {
					currWord = snapshot.value.toString().toInt()
					puzzle = Puzzle(room.puzzle)
					val guessWord = puzzle.wordsList[currWord].toString()

					for (i in (currChar+1) until guessWord.length) {
						val resID = resources.getIdentifier("tv_grid_${currWord}${i}" + "o", "id",
						                                    packageName)
						val tv = findViewById<View>(resID) as TextView
						tv.setText(guessWord[i].toString())
					}
					currChar = -1
					currWord++  // move to next word
				}

			}


			override fun onCancelled(error: DatabaseError) { //					Toast.makeText(this@OfflineGameScreen, error.toString(), Toast.LENGTH_SHORT).show()
			}

		})


		// For changing truns
		var ref2 = database.getReference("ROOM").child(roomId).child("turn")
		ref2.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				turn = snapshot.value.toString().toInt()
				if ((turn == 0 && player == 1) || (turn == 1 && player == 2)) {
					binding.tvTurno.text = "Your Turn"
					binding.btnGuesso.visibility = VISIBLE
					binding.etGuesso.visibility = VISIBLE
				}
				else {
					binding.tvTurno.text = "Opponent's Turn"
					binding.btnGuesso.visibility = INVISIBLE
					binding.etGuesso.visibility = INVISIBLE
				}

			}


			override fun onCancelled(error: DatabaseError) { //					Toast.makeText(this@OfflineGameScreen, error.toString(), Toast.LENGTH_SHORT).show()
			}

		})

		// For displaying one character
		var ref3 = database.getReference("ROOM").child(roomId).child("charNo")
		ref3.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				if (this@OnlineGameScreen::puzzle.isInitialized )
				{val guessWord = puzzle.wordsList[currWord].toString()
				var charNo = snapshot.value.toString().toInt()
				if (charNo != -1) {
					if (guessWord.length - 1 == charNo) {
						Toast.makeText(this@OnlineGameScreen, "Last character cannot be displayed", Toast.LENGTH_SHORT).show()
						return
					}


					val resID = resources.getIdentifier("tv_grid_${currWord}${charNo}" + "o", "id",
					                                    packageName)
					val tv = findViewById<View>(resID) as TextView
					tv.setText(guessWord[charNo].toString())
					currChar=charNo

				}
			}}


			override fun onCancelled(error: DatabaseError) { //					Toast.makeText(this@OfflineGameScreen, error.toString(), Toast.LENGTH_SHORT).show()
			}

		})

	}

	/** Game setup - fetch a puzzle from database and set the first and last word **/
	private fun GameSetup() {

		animView.pauseAnimation()        //soundEffect.pause()

		binding.tvWaitmessageo.text = "Room code: " + room.id
		puzzle = Puzzle(room.puzzle)
		var row = 0
		for (pz in puzzle.wordsList) {
			if (row == 0 || row == 4) {
				pz as String
				for (col in 0 until pz.length) {
					val resID = resources.getIdentifier("tv_grid_$row$col" + "o", "id", packageName)
					val tv = findViewById<View>(resID) as TextView
					tv.setText(pz[col].toString())
				}
			}
			row++
		}

	}

	private fun startGame() {
		binding.tvWaitmessageo.visibility = INVISIBLE

		if ((turn == 0 && player == 1) || (turn == 1 && player == 2)) {
			binding.tvTurno.text = "Your Turn"
			binding.btnGuesso.visibility = VISIBLE
			binding.etGuesso.visibility = VISIBLE
		}
		else {
			binding.tvTurno.text = "Opponent's Turn"
			binding.btnGuesso.visibility = INVISIBLE
			binding.etGuesso.visibility = INVISIBLE
		}
	}



	/** If the user enters a wrong guess - show a new single character **/
	private fun ShowOneChar() { //		val guessWord = puzzle.wordsList[currWord].toString()
		//
		//		if(guessWord.length-1 == currChar)
		//		{
		//			Toast.makeText(this, "no beta", Toast.LENGTH_SHORT).show()
		//			return
		//		}
		//
		//
		//		val resID = resources.getIdentifier(
		//			"tv_grid_${currWord}${currChar}"+"o",
		//			"id", packageName
		//		                                   )
		//		val tv = findViewById<View>(resID) as TextView
		//		tv.setText(guessWord[currChar].toString())
		currChar++  // next char
		database.getReference("ROOM").child(roomId).child("charNo").setValue(currChar)


	}

	/** If the player guesses the word - complete word is displayed **/
	private fun CompleteWord() { //		val guessWord = puzzle.wordsList[currWord].toString()
		//
		//		for(i in currChar until guessWord.length)
		//		{
		//			val resID = resources.getIdentifier(
		//				"tv_grid_${currWord}${i}"+"o",
		//				"id", packageName
		//			                                   )
		//			val tv = findViewById<View>(resID) as TextView
		//			tv.setText(guessWord[i].toString())
		//		}
		//

		database.getReference("ROOM").child(roomId).child("wordNo").setValue(currWord)

		currChar = -1    // from the start of the word
		database.getReference("ROOM").child(roomId).child("charNo").setValue(currChar)

		if (currWord == 3) {
			Toast.makeText(this@OnlineGameScreen, "Finished", Toast.LENGTH_SHORT).show()
			val i = Intent(this, MainMenu::class.java)
			startActivity(i)
			finishAffinity()
		}

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
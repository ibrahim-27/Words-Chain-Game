package com.example.wordchain

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.wordchain.Models.Puzzle
import com.example.wordchain.Models.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class OnlineHome : AppCompatActivity() {

	/** Firebase references **/
	val database = Firebase.database
	val auth = FirebaseAuth.getInstance()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_online_home)

		if(auth.currentUser == null){
			var intent=Intent(this,MainMenu::class.java)
			startActivity(intent)
		}

		var crbtn=findViewById<Button>(R.id.btn_createroom)
		crbtn.setOnClickListener(){
			lateinit var puzzle:Puzzle
			var pzl= arrayListOf<String>()
			val random: Int = java.util.Random().nextInt(4)
			val ref = database.getReference("Puzzles").child(random.toString())
			ref.addValueEventListener(object: ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					puzzle = Puzzle(snapshot.getValue() as ArrayList<*>)

					var row = 0
					for (pz in puzzle.wordsList)
					{
						pz as String
						pzl.add(pz)

					}
					val myRef = database.getReference("ROOM").push()
					var id =myRef.key!!
					var a=id.substring(1,3)
					id=a+id.substring(id.length-5,id.length)
					var turn:Long= (0..1).random().toLong()
					var room= Room(id, auth.currentUser!!.uid,turn ,pzl)
					myRef.setValue(room)

					intent = Intent(this@OnlineHome,OnlineGameScreen::class.java )
					intent.putExtra("key",myRef.key!!)
					startActivity(intent)
				}

				override fun onCancelled(error: DatabaseError) {

				}

			})
		}
		var jrbtn=findViewById<Button>(R.id.btn_joinroom)
		jrbtn.setOnClickListener(){
			showDialog()
		}

	}
	private fun showDialog(){
		val builder=AlertDialog.Builder(this)
		val inflator=layoutInflater
		val dialogLayout=inflator.inflate(R.layout.join_room_dialog,null)
		val et_jr=dialogLayout.findViewById<EditText>(R.id.et_jr)
		var id=""

		with(builder){
			setTitle("Enter Room Id")
			setPositiveButton("Join"){dialog,which ->
				id=et_jr.text.toString()
				Toast.makeText(this@OnlineHome,id,Toast.LENGTH_LONG).show()


				// All food donated in the same city
				val ref2 = database.getReference("ROOM")
				ref2.addValueEventListener(object : ValueEventListener {
					override fun onDataChange(snapshot: DataSnapshot) {

						for (room in snapshot.children) {


							var sid=room.key!!.toString().substring(1,3)
							sid=sid+room.key!!.toString().substring(room.key!!.toString().length-5,room.key!!.toString().length)
							if (sid == id ) {
								if(room.child("player1_id").value.toString()!=auth.uid.toString()){
								id=room.child("id").value.toString()
								database.getReference("ROOM").child(room.key!!.toString()).child("player2_id").setValue(auth.uid.toString())
								intent= Intent(this@OnlineHome,OnlineGameScreen::class.java )
								intent.putExtra("key",room.key!!)
								startActivity(intent)
								}else{
									Toast.makeText(this@OnlineHome,"You can not join a Room twice!",Toast.LENGTH_LONG).show()
								}
							}
						}
					}
					override fun onCancelled(error: DatabaseError) {
						Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
					}
				})




			}
			setNegativeButton("Cancel"){dialog,which->
				Log.d("Message","Pressed cancel")
				id="cancel"
			}
			setView(dialogLayout)
			show()
		}

	}

}
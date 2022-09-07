package com.example.wordchain.Models

class Room {


		var id: String = ""
		var Player1_id: String = ""
		var Player2_id: String = ""
		var turn: Long = 0
		var puzzle: ArrayList<String>

	var wordNo:Long=0
	var charNo:Long=-1


		constructor(id: String, Player1_id: String, turn: Long, Puzzle: ArrayList<String>) {
			this.id = id
			this.Player1_id = Player1_id
			this.turn=turn
			this.puzzle = Puzzle
		}

	constructor(id: String, Player1_id: String,Player2_id: String ,turn:Long, Puzzle: ArrayList<String>,wordno:Long,charNo:Long){
		this.id = id
		this.Player1_id = Player1_id
		this.turn=turn
		this.puzzle = Puzzle
		this.Player2_id=Player2_id
		this.wordNo=wordno
		this.charNo=charNo
	}
	}

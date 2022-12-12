package io.wollinger.rataudc.match

import io.wollinger.rataudc.Utils
import java.lang.Exception
import kotlin.concurrent.thread

class Match(val inviteLink: String) {
    enum class STATE { P1_TURN, P2_TURN, END }

    var state = if((0..1).random() == 0) STATE.P1_TURN else STATE.P2_TURN

    fun isMyTurn(player: MatchPlayer): Boolean {
        if(state == STATE.P1_TURN && player.userID == player1!!.userID) return true
        if(state == STATE.P2_TURN && player.userID == player2!!.userID) return true
        return false
    }

    fun betterPlayer(): MatchPlayer? {
        val sc1 = player1!!.board.calculateScore()
        val sc2 = player2!!.board.calculateScore()
        return if(sc1 == sc2) null else if(sc1 > sc2) player1!! else player2!!
    }

    private var timeout = 60000 * 10 //10 minutes
    private var lastUpdated: Long = Utils.currentTime()
    private fun refreshLastUpdated() = kotlin.run { lastUpdated = Utils.currentTime() }

    private var player1: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            refreshLastUpdated()
        }
    private var player2: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            refreshLastUpdated()
        }

    fun endGame() {
        if(state == STATE.END) return

        state = STATE.END
        if(isFull()) {
            player1!!.refreshUpdateMessage("Game end")
            player2!!.refreshUpdateMessage("Game end")
            fun s(p: MatchPlayer) {
                p.channel.sendMessage("https://static.wikia.nocookie.net/cult-of-the-lamb/images/0/02/Ratau-knucklebones-win-game-loop.gif").queue()
                p.channel.sendMessage("Thanks for playing!").queue()
            }
            s(player1!!)
            s(player2!!)
        }
        fun bye(p: MatchPlayer?) {
            if(p != null) MatchManager.leave(p.userID)
        }
        bye(player1)
        bye(player2)
    }

    //Returns true if sent
    fun handleMessage(playerID: Long, message: String): Boolean {
        fun s(p: MatchPlayer, o: MatchPlayer): Boolean {
            if(p.userID != playerID) return false
            o.channel.sendMessage("${player1!!.username}: $message").queue()
            return true
        }
        if(!isFull()) return false
        return s(player1!!, player2!!) || s(player2!!, player1!!)
    }

    fun buttonEvent(playerID: Long, buttonID: String) {
        val player = when(playerID) {
            player1!!.userID -> if(state == STATE.P1_TURN || buttonID == "roll") player1 else return
            player2!!.userID -> if(state == STATE.P2_TURN || buttonID == "roll") player2 else return
            else -> throw Exception("Bad id: $playerID")
        }!!

        when(buttonID) {
            "roll" -> player.roll()
            "p1", "p2", "p3" -> {
                val column = buttonID.replaceFirst("p", "").toInt() - 1
                player.addPiece(column)

                if(state == STATE.P1_TURN) state = STATE.P2_TURN
                else if(state == STATE.P2_TURN) state = STATE.P1_TURN
                player1!!.refreshUpdateMessage("Button press of $player")
                player1!!.updateDiceTray("Button press of $player")
                player2!!.refreshUpdateMessage("Button press of $player")
                player2!!.updateDiceTray("Button press of $player")
            }
        }
        refreshLastUpdated()
    }

    private fun isDone(): Boolean {
        fun doneCheck(matchPlayer: MatchPlayer): Boolean {
            var hasSpace = false
            for(i in 0..2)
                if(matchPlayer.board.hasSpace(i))
                    hasSpace = true
            return !hasSpace
        }
        return doneCheck(player1!!) || doneCheck(player2!!)
    }

    private fun checkIfStart() {
        if(player1 != null && player2 != null) {
            fun setup(p1: MatchPlayer, p2: MatchPlayer) {
                thread {
                    Thread.currentThread().name = "InitBoard-$p1-$p2"
                    p1.setupBoard(p2)
                    p1.board.changeListener = {
                        refreshLastUpdated()
                        //Update opponent with our new board and check if there have been any updates
                        val didChangeOpponent = p2.updateFromOpponent(p1)
                        //Update said opponents boards message
                        p2.updateOpponentBoard(p1, "Board modified of $p1")
                        p2.updateBoard("Board modified of $p1")
                        p2.updateDiceTray("Board modified of $p1")
                        //If opponents board changed with our change update our own opponentMessage as well
                        if(didChangeOpponent) p1.updateOpponentBoard(p2, "Board modified of $p1 - Caused change in board of $p2")

                        if(isDone())
                            endGame()
                    }
                }
            }
            setup(player1!!, player2!!)
            setup(player2!!, player1!!)
            refreshLastUpdated()
        }
    }

    fun checkLastUpdated() {
        val timePassed = Utils.currentTime() - lastUpdated
        if(timePassed >= timeout) {
            fun e(p: MatchPlayer?) {
                if(p == null) return
                p.channel.sendMessage("No updates in a while. Killing match").queue()
                endGame()
            }
            e(player1)
            e(player2)
        }
    }

    fun addPlayer(player: MatchPlayer) {
        if(player1 == null) player1 = player
        else if(player2 == null) player2 = player
    }

    fun isInMatch(userID: Long) = userID == player1?.userID || userID == player2?.userID

    fun isFull() = player1 != null && player2 != null

    fun log(message: String) = println("M-${inviteLink.slice(0..5)}: $message")

    override fun toString() = "Match(player1=$player1, player2=$player2, inviteLink=$inviteLink)"
}
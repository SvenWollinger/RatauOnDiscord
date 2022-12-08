package io.wollinger.rataudc.match

import io.wollinger.rataudc.Utils
import java.lang.Exception
import kotlin.concurrent.thread

class Match {

    private var timeout = 120000
    private var lastUpdated: Long = Utils.currentTime()
    fun refreshLastUpdated() = kotlin.run { lastUpdated = Utils.currentTime() }

    var inviteLink: String? = null
    var player1: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            refreshLastUpdated()
        }
    var player2: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            refreshLastUpdated()
        }

    fun endGame() {
        fun del(p: MatchPlayer) {
            p.boardMessage.delete().queue()
            p.opponentBoardMessage.delete().queue()
            p.rollMessage.delete().queue()
            p.otherMessages.forEach { it.delete().queue() }
        }
        del(player1!!)
        del(player2!!)
    }

    fun buttonEvent(playerID: Long, buttonID: String) {
        val player = when(playerID) {
            player1!!.userID -> player1
            player2!!.userID -> player2
            else -> throw Exception("Bad id: $playerID")
        }!!
        when(buttonID) {
            "roll" -> player.roll()
            "p1" -> player.addPiece(0)
            "p2" -> player.addPiece(1)
            "p3" -> player.addPiece(2)
        }
        refreshLastUpdated()
    }

    private fun checkIfStart() {
        if(player1 != null && player2 != null) {
            fun setup(p1: MatchPlayer, p2: MatchPlayer) {
                thread {
                    Thread.currentThread().name = "InitBoard-$p1-$p2"
                    p1.setupBoard(p2)
                    p1.boardChangeListener = {
                        refreshLastUpdated()
                        //Update opponent with our new board and check if there have been any updates
                        val didChangeOpponent = p2.updateFromOpponent(p1)
                        //Update said opponents boards message
                        p2.updateOpponentBoard(p1)
                        p2.updateBoard()
                        //If opponents board changed with our change update our own opponentMessage as well
                        if(didChangeOpponent) p1.updateOpponentBoard(p2)
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
                MatchManager.leave(p.userID)
            }
            e(player1)
            e(player2)
        }
    }
}
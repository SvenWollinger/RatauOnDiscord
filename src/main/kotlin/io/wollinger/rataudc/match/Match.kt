package io.wollinger.rataudc.match

import io.wollinger.rataudc.Utils
import java.lang.Exception
import kotlin.concurrent.thread

class Match {

    private var timeout = 120000
    private var lastUpdated: Long = Utils.currentTime()
    var inviteLink: String? = null
    var player1: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            lastUpdated = Utils.currentTime()
        }
    var player2: MatchPlayer? = null
        set(value) {
            field = value
            checkIfStart()
            lastUpdated = Utils.currentTime()
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
    }

    private fun checkIfStart() {
        if(player1 != null && player2 != null) {
            thread {
                Thread.currentThread().name = "InitBoard-$player1"
                player1!!.setupBoard(player2!!)
                player1!!.boardChangeListener = {
                    player2!!.updateOpponentBoard(player1!!)
                }
            }
            thread {
                Thread.currentThread().name = "InitBoard-$player2"
                player2!!.setupBoard(player1!!)
                player2!!.boardChangeListener = {
                    player1!!.updateOpponentBoard(player2!!)
                }
            }
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
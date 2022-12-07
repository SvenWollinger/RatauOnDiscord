package io.wollinger.rataudc

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.lang.Exception
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class MatchPlayer(private val match: Match, val userID: Long, val channel: MessageChannel) {
    var username = Ratau.jda.retrieveUserById(userID).complete().name
    lateinit var opponentBoardMessage: Message
    lateinit var boardMessage: Message
    lateinit var rollMessage: Message
    val otherMessages = ArrayList<Message>()
    private var roll: Int = 0

    private val boardSize = 256
    private val textHeight = 64
    private val rollThingWidth = 96

    private var changeListener: ((MatchPlayer) -> Unit)? = null

    private var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    fun getPiece(x: Int, y: Int) = board[y][x]
    fun setPiece(x: Int, y: Int, piece: Int) {
        board[y][x] = piece
        changeListener?.invoke(this)
    }

    fun updateOpponentBoard(opponent: MatchPlayer) {
        opponentBoardMessage.setImage(opponent.renderBoard(boardSize, boardSize, true)).queue()
    }

    private fun updateBoard() {
        boardMessage.setImage(renderBoard(boardSize, boardSize)).queue()
    }

    fun hasSpace(column: Int) = getPiece(column, 0) == 0 || getPiece(column, 1) == 0 || getPiece(column, 2) == 0
    fun addPiece(column: Int) {
        if(roll == 0) return

        for(y in 0..2) {
            if(getPiece(column, y) == 0 && roll != 0) {
                setPiece(column, y, roll)
                roll = 0
                updateBoard()
                updateRollThing()
            }
        }
    }

    fun calculateColumnScore(column: Int): Int {
        val numbers = CopyOnWriteArrayList<Int>()
        for(i in 0..2) numbers.add(board[i][column])

        var sum = 0
        for(i in numbers) {
            when(Collections.frequency(numbers, i)) {
                1 -> sum+=i
                2 ->  {
                    sum += i * 4
                    repeat(2) { numbers.remove(i) }
                }
                3 -> return i * 3 * 3
            }
        }
        return sum;
    }

    private fun updateRollThing() {
        fun b(btn: Button, b: Boolean) = if(b) btn else btn.asDisabled()
        rollMessage.setImage(Utils.renderDiceWithBG(roll, rollThingWidth, textHeight)).setContent("").setActionRow(
            b(Button.secondary("${match.inviteLink}-${userID}-roll", Emoji.fromUnicode("\uD83C\uDFB2")), roll == 0),
            b(Button.secondary("${match.inviteLink}-${userID}-p1", Emoji.fromUnicode("1️⃣")), roll != 0 && hasSpace(0)),
            b(Button.secondary("${match.inviteLink}-${userID}-p2", Emoji.fromUnicode("2️⃣")), roll != 0 && hasSpace(1)),
            b(Button.secondary("${match.inviteLink}-${userID}-p3", Emoji.fromUnicode("3️⃣")), roll != 0 && hasSpace(2))
        ).complete()
    }

    fun roll() {
        roll = (1..6).random()
        updateRollThing()
    }

    fun setupBoard(opponent: MatchPlayer) {
        otherMessages.add(channel.sendMessage("${username} VS ${opponent.username}").complete())
        otherMessages.add(channel.sendFiles(Utils.renderStringToImage(opponent.username, boardSize, textHeight).toFileUpload()).complete())
        opponentBoardMessage = channel.sendFiles(opponent.renderBoard(boardSize, boardSize, true).toFileUpload()).complete()
        otherMessages.add(channel.sendFiles(Utils.renderStringToImage("", boardSize, textHeight / 2).toFileUpload()).complete())
        boardMessage = channel.sendFiles(renderBoard(boardSize, boardSize).toFileUpload()).complete()
        //otherMessages.add(channel.sendFiles(Utils.renderStringToImage("Your roll:", 128, textHeight).toFileUpload()).complete())
        rollMessage = channel.sendMessage("roll").complete()
        updateRollThing()
    }

    fun renderBoard(width: Int, height: Int, mirrored: Boolean = false): BufferedImage {
        val scoreSize = textHeight / 3
        return BufferedImage(width, height + scoreSize, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            val cellWidth = width / 3
            val cellHeight = height / 3

            fun r(x: Int, y: Int, str: Int) {
                val i = Utils.renderStringToImage(str.toString(), cellWidth, scoreSize)
                g.drawImage(i, x, y, null)
            }

            if(!mirrored) {
                r(0, 0, calculateColumnScore(0))
                r(cellWidth, 0, calculateColumnScore(1))
                r(cellWidth * 2, 0, calculateColumnScore(2))
            }

            for(y in 0 until 3) {
                for(x in 0 until 3) {
                    val piece = if(!mirrored) getPiece(x, y) else getPiece(x, 2 - y)
                    val addX = if(!mirrored) scoreSize else 0
                    g.drawImage(Utils.renderDiceWithBG(piece, cellWidth, cellHeight), x * cellWidth, y * cellHeight + addX, null)
                }
            }

            if(mirrored) {
                r(0, height, calculateColumnScore(0))
                r(cellWidth, height, calculateColumnScore(1))
                r(cellWidth * 2, height, calculateColumnScore(2))
            }
        }
    }

    fun setBoardChangeListener(action: (MatchPlayer) -> Unit) {
        changeListener = action
    }

    override fun toString() = "MatchPlayer(name=$username, id=$userID)"
}

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
                player1!!.setBoardChangeListener {
                    player2!!.updateOpponentBoard(player1!!)
                }
            }
            thread {
                Thread.currentThread().name = "InitBoard-$player2"
                player2!!.setupBoard(player1!!)
                player2!!.setBoardChangeListener {
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

object MatchManager {
    enum class Result {SUCCESS, NOT_FOUND, SELF_JOIN_ERROR, HAS_RUNNING_MATCH}
    class Response(val result: Result, val content: Any? = null)

    private val inviteMatches = HashMap<String, Match>()
    private val usersMatches = HashMap<Long, Match>()
    private var servicesStarted = false

    fun startServices() {
        if(servicesStarted) return
        servicesStarted = true

        thread {
            Thread.currentThread().name = "MatchLastUpdateService"
            while(true) {
                inviteMatches.forEach { (_, u) -> u.checkLastUpdated() }
                Thread.sleep(1000)
            }
        }
    }

    fun createInviteMatch(userID: Long, channel: MessageChannel): Response {
        if(usersMatches.containsKey(userID)) return Response(Result.HAS_RUNNING_MATCH)

        val inviteLink = Utils.getInviteLink(userID)
        Match().also {
            it.inviteLink = inviteLink
            it.player1 = MatchPlayer(it, userID, channel)
            inviteMatches[inviteLink] = it
            usersMatches[userID] = it
            println("Match created: Link($inviteLink) ${it.player1}")
        }
        return Response(Result.SUCCESS, inviteLink)
    }

    fun getMatch(inviteLink: String): Match? = inviteMatches[inviteLink]

    fun joinMatch(inviteLink: String, userID: Long, channel: MessageChannel): Response {
        if(!inviteMatches.containsKey(inviteLink)) return Response(Result.NOT_FOUND)
        inviteMatches[inviteLink]!!.also {
            if(it.player1!!.userID == userID) return Response(Result.SELF_JOIN_ERROR)
            it.player2 = MatchPlayer(it, userID, channel)
            usersMatches[userID] = it
        }
        return Response(Result.SUCCESS)
    }

    fun leave(userID: Long): Response {
        usersMatches[userID]?.also {
            usersMatches.remove(userID)
            inviteMatches.remove(it.inviteLink)
            return Response(Result.SUCCESS)
        }
        return Response(Result.NOT_FOUND)
    }
}
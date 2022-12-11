package io.wollinger.rataudc.match

import io.wollinger.rataudc.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.lang.Exception
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList

class MatchPlayer(private val match: Match, val userID: Long, val channel: MessageChannel) {
    val username = Ratau.jda.retrieveUserById(userID).complete().name
    private lateinit var opponentBoardMessage: Message
    private lateinit var boardMessage: Message
    private lateinit var rollMessage: Message
    private lateinit var updateMessage: Message
    private val otherMessages = ArrayList<Message>()
    private var roll: Int = 0

    private val boardSize = 256
    private val textHeight = 64
    private val rollThingWidth = 96
    val board = MatchBoard()

    fun updateOpponentBoard(opponent: MatchPlayer) {
        opponentBoardMessage.setImage(opponent.renderBoard(boardSize, boardSize, true)).queue()
    }

    fun updateBoard() {
        boardMessage.setImage(renderBoard(boardSize, boardSize)).queue()
    }

    fun updateFromOpponent(opponent: MatchPlayer): Boolean {
        var didDestroy = false
        for(x in 0..2) {
            for(y in 0..2) {
                if(board.destroyPieces(x, opponent.board.getPiece(x, y)))
                    didDestroy = true
            }
        }
        return didDestroy
    }

    fun addPiece(column: Int) {
        if(board.addPiece(column, roll)) {
            roll = 0
            updateBoard()
            updateRollThing()
        }
    }

    private fun getPieceColor(column: Int, piece: Int): Color? {
        if(piece == 0) return null
        val numbers = CopyOnWriteArrayList<Int>()
        for(i in 0..2) numbers.add(board.getPiece(column, i))

        return when(Collections.frequency(numbers, piece)) {
            1 -> null
            2 -> Color(255, 255, 0, 100)
            3 -> Color(0, 0, 255, 100)
            else -> throw Exception(":(")
        }
    }

    fun updateRollThing() {
        fun b(btn: Button, b: Boolean) = if(b) btn else btn.asDisabled()
        val isOurTurn = match.isMyTurn(this)
        rollMessage.setImage(Utils.renderDiceWithBG(roll, rollThingWidth, textHeight)).setContent("").setActionRow(
            b(Button.secondary("${match.inviteLink}-${userID}-roll", Emoji.fromUnicode("\uD83C\uDFB2")), roll == 0),
            b(Button.secondary("${match.inviteLink}-${userID}-p1", Emoji.fromUnicode("1️⃣")), roll != 0 && board.hasSpace(0) && isOurTurn),
            b(Button.secondary("${match.inviteLink}-${userID}-p2", Emoji.fromUnicode("2️⃣")), roll != 0 && board.hasSpace(1) && isOurTurn),
            b(Button.secondary("${match.inviteLink}-${userID}-p3", Emoji.fromUnicode("3️⃣")), roll != 0 && board.hasSpace(2) && isOurTurn)
        ).complete()
    }

    fun roll() {
        roll = (1..6).random()
        updateRollThing()
    }

    fun refreshUpdateMessage() {
        val str = if(match.state == Match.STATE.END) {
            when (val won = match.betterPlayer()) {
                null -> "Tie!"
                this -> "You won!"
                else -> "${won.username} won!"
            }
        } else {
            if(match.isMyTurn(this)) "Your turn" else ""
        }
        updateMessage.setImage(Utils.renderStringToImage(str, boardSize, textHeight / 2)).complete()
    }

    fun setupBoard(opponent: MatchPlayer) {
        otherMessages.add(channel.sendMessage("$username VS ${opponent.username}").complete())
        otherMessages.add(channel.sendFiles(Utils.renderStringToImage(opponent.username, boardSize, textHeight).toFileUpload()).complete())
        opponentBoardMessage = channel.sendFiles(opponent.renderBoard(boardSize, boardSize, true).toFileUpload()).complete()
        updateMessage = channel.sendFiles(Utils.renderStringToImage("", boardSize, textHeight / 2).toFileUpload()).complete()
        boardMessage = channel.sendFiles(renderBoard(boardSize, boardSize).toFileUpload()).complete()
        rollMessage = channel.sendMessage("roll").complete()
        updateRollThing()
        refreshUpdateMessage()
    }

    private fun renderBoard(width: Int, height: Int, mirrored: Boolean = false): BufferedImage {
        val scoreSize = textHeight / 3
        return BufferedImage(width, height + scoreSize * 2, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            val cellWidth = width / 3
            val cellHeight = height / 3

            fun r(x: Int, y: Int, column: Int) {
                val i = Utils.renderStringToImage(board.calculateColumnScore(column).toString(), cellWidth, scoreSize)
                g.drawImage(i, x, y, null)
            }

            if(!mirrored) {
                g.drawImage(Utils.renderStringToImage(board.calculateScore().toString(), width, scoreSize), 0, 0, null)

                r(0, scoreSize, 0)
                r(cellWidth, scoreSize, 1)
                r(cellWidth * 2, scoreSize, 2)
            }

            for(y in 0 until 3) {
                for(x in 0 until 3) {
                    val piece = if(!mirrored) board.getPiece(x, y) else board.getPiece(x, 2 - y)
                    val addX = if(!mirrored) scoreSize * 2 else 0
                    val color = getPieceColor(x, piece)
                    g.drawImage(Utils.renderDiceWithBG(piece, cellWidth, cellHeight, color), x * cellWidth, y * cellHeight + addX, null)
                }
            }

            if(mirrored) {
                r(0, height, 0)
                r(cellWidth, height, 1)
                r(cellWidth * 2, height, 2)

                g.drawImage(Utils.renderStringToImage(board.calculateScore().toString(), width, scoreSize), 0, height + scoreSize, null)
            }
        }
    }

    override fun toString() = "MatchPlayer(name=$username, id=$userID)"
}
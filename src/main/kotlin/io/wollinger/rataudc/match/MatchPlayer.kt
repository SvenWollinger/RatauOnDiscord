package io.wollinger.rataudc.match

import io.wollinger.rataudc.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList

class MatchPlayer(private val match: Match, val userID: Long, val channel: MessageChannel) {
    private var username = Ratau.jda.retrieveUserById(userID).complete().name
    lateinit var opponentBoardMessage: Message
    lateinit var boardMessage: Message
    lateinit var rollMessage: Message
    lateinit var updateMessage: Message
    val otherMessages = ArrayList<Message>()
    private var roll: Int = 0

    private val boardSize = 256
    private val textHeight = 64
    private val rollThingWidth = 96

    var boardChangeListener: ((MatchPlayer) -> Unit)? = null

    private var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    fun getPiece(x: Int, y: Int) = board[y][x]
    fun setPiece(x: Int, y: Int, piece: Int, quietChange: Boolean = false) {
        board[y][x] = piece
        if(!quietChange) boardChangeListener?.invoke(this)
    }

    fun updateOpponentBoard(opponent: MatchPlayer) {
        opponentBoardMessage.setImage(opponent.renderBoard(boardSize, boardSize, true)).queue()
    }

    fun updateBoard() {
        boardMessage.setImage(renderBoard(boardSize, boardSize)).queue()
    }

    //Returns true if we did destroy something
    fun destroyPieces(column: Int, piece: Int): Boolean {
        if(piece == 0) return false
        var didDestroy = false
        for (i in 0..2) {
            if (getPiece(column, i) == piece) {
                //Quiet change, we dont want this to cause any updates by itself visually
                setPiece(column, i, 0, true)
                didDestroy = true
            }
        }
        return didDestroy
    }

    fun updateFromOpponent(opponent: MatchPlayer): Boolean {
        var didDestroy = false
        for(x in 0..2) {
            for(y in 0..2) {
                if(destroyPieces(x, opponent.getPiece(x, y)))
                    didDestroy = true
            }
        }
        return didDestroy
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

    fun calculateScore(): Int {
        var totalScore = 0
        for(i in 0..2) totalScore += calculateColumnScore(i)
        return totalScore
    }

    fun calculateColumnScore(column: Int): Int {
        val numbers = CopyOnWriteArrayList<Int>()
        for(i in 0..2) numbers.add(board[i][column])

        var sum = 0
        for(i in numbers) {
            when(Collections.frequency(numbers, i)) {
                1 -> sum += i
                2 ->  {
                    sum += i * 4
                    repeat(2) { numbers.remove(i) }
                }
                3 -> return i * 3 * 3
            }
        }
        return sum
    }

    fun updateRollThing() {
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

    fun refreshUpdateMessage() {
        val str = if(match.isMyTurn(this)) "Your turn" else ""
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

    fun renderBoard(width: Int, height: Int, mirrored: Boolean = false): BufferedImage {
        val scoreSize = textHeight / 3
        return BufferedImage(width, height + scoreSize * 2, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            val cellWidth = width / 3
            val cellHeight = height / 3

            fun r(x: Int, y: Int, str: Int) {
                val i = Utils.renderStringToImage(str.toString(), cellWidth, scoreSize)
                g.drawImage(i, x, y, null)
            }

            if(!mirrored) {
                g.drawImage(Utils.renderStringToImage(calculateScore().toString(), width, scoreSize), 0, 0, null)

                r(0, scoreSize, calculateColumnScore(0))
                r(cellWidth, scoreSize, calculateColumnScore(1))
                r(cellWidth * 2, scoreSize, calculateColumnScore(2))
            }

            for(y in 0 until 3) {
                for(x in 0 until 3) {
                    val piece = if(!mirrored) getPiece(x, y) else getPiece(x, 2 - y)
                    val addX = if(!mirrored) scoreSize * 2 else 0
                    g.drawImage(Utils.renderDiceWithBG(piece, cellWidth, cellHeight), x * cellWidth, y * cellHeight + addX, null)
                }
            }

            if(mirrored) {
                r(0, height, calculateColumnScore(0))
                r(cellWidth, height, calculateColumnScore(1))
                r(cellWidth * 2, height, calculateColumnScore(2))

                g.drawImage(Utils.renderStringToImage(calculateScore().toString(), width, scoreSize), 0, height + scoreSize, null)
            }
        }
    }

    override fun toString() = "MatchPlayer(name=$username, id=$userID)"
}
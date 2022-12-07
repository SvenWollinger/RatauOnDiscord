package io.wollinger.rataudc

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.concurrent.thread

class MatchPlayer(val userID: Long, val channel: MessageChannel) {
    var username = Ratau.jda.retrieveUserById(userID).complete().name
    lateinit var opponentBoardMessage: Message
    lateinit var boardMessage: Message
    lateinit var rollMessage: Message
    val otherMessages = ArrayList<Message>()

    private var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    fun getPiece(x: Int, y: Int) = board[y][x]
    fun setPiece(x: Int, y: Int, piece: Int) = kotlin.run { board[y][x] = piece }

    fun renderBoard(width: Int, height: Int): BufferedImage {
        return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            val cellWidth = width / 3
            val cellHeight = height / 3
            for(y in 0 until 3) {
                for(x in 0 until 3) {
                    g.drawImage(Utils.renderDiceWithBG(getPiece(x, y), cellWidth, cellHeight), x * cellWidth, y * cellHeight, null)
                }
            }
        }
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

    private fun checkIfStart() {
        if(player1 != null && player2 != null) {
            fun prepareBoard(p1: MatchPlayer, p2: MatchPlayer) {
                thread {
                    val boardSize = 256
                    val textHeight = 64
                    Thread.currentThread().name = "InitBoard-$p1-$p2"
                    p1.otherMessages.add(p1.channel.sendMessage("${p1.username} VS ${p2.username}").complete())
                    p1.otherMessages.add(p1.channel.sendFiles(Utils.renderStringToImage(p2.username, boardSize, textHeight).toFileUpload()).complete())
                    p1.opponentBoardMessage = p1.channel.sendFiles(p2.renderBoard(boardSize, boardSize).toFileUpload()).complete()
                    p1.otherMessages.add(p1.channel.sendFiles(Utils.renderStringToImage(p1.username, boardSize, textHeight).toFileUpload()).complete())
                    p1.boardMessage = p1.channel.sendFiles(p1.renderBoard(boardSize, boardSize).toFileUpload()).complete()
                    p1.otherMessages.add(p1.channel.sendFiles(Utils.renderStringToImage("Your roll:", 128, textHeight).toFileUpload()).complete())
                    p1.rollMessage = p1.channel.sendFiles(Utils.renderDiceWithBG(0, 64, textHeight).toFileUpload()).addActionRow(
                        Button.success("roll", Emoji.fromUnicode("\uD83C\uDFB2")),
                        Button.primary("p1", Emoji.fromUnicode("1️⃣")),
                        Button.primary("p2", Emoji.fromUnicode("2️⃣")),
                        Button.primary("p3", Emoji.fromUnicode("3️⃣"))
                    ).complete()
                }
            }
            prepareBoard(player1!!, player2!!)
            prepareBoard(player2!!, player1!!)
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
            it.player1 = MatchPlayer(userID, channel)
            inviteMatches[inviteLink] = it
            usersMatches[userID] = it
            println("Match created: Link($inviteLink) ${it.player1}")
        }
        return Response(Result.SUCCESS, inviteLink)
    }

    fun joinMatch(inviteLink: String, userID: Long, channel: MessageChannel): Response {
        if(!inviteMatches.containsKey(inviteLink)) return Response(Result.NOT_FOUND)
        inviteMatches[inviteLink]!!.also {
            if(it.player1!!.userID == userID) return Response(Result.SELF_JOIN_ERROR)
            it.player2 = MatchPlayer(userID, channel)
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
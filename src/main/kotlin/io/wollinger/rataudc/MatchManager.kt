package io.wollinger.rataudc

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import okhttp3.internal.threadName
import kotlin.concurrent.thread

class MatchPlayer(val userID: Long, val channel: MessageChannel) {
    var username = Ratau.jda.retrieveUserById(userID).complete().name

    var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

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

    private fun checkIfStart() {
        if(player1 != null && player2 != null) {
            println("Match started: $player1 vs $player2")
            player1!!.channel.sendMessage("Starting match with $player2").queue()
            player2!!.channel.sendMessage("Starting match with $player1").queue()
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
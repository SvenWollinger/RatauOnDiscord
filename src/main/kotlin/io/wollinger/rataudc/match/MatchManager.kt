package io.wollinger.rataudc.match

import io.wollinger.rataudc.Utils
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import kotlin.collections.HashMap
import kotlin.concurrent.thread


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
package io.wollinger.rataudc.match

import io.wollinger.rataudc.Utils
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import kotlin.collections.HashMap
import kotlin.concurrent.thread


object MatchManager {
    enum class Result {SUCCESS, NOT_FOUND, SELF_JOIN_ERROR, HAS_RUNNING_MATCH, MATCH_FULL}
    class Response(val result: Result, val content: Any? = null)

    private val inviteMatches = HashMap<String, Match>()
    private val usersMatches = HashMap<Long, Match>()
    private var servicesStarted = false

    //Starts the services
    // MatchLastUpdateService -> Takes care of ending matches after x amount of time
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

    //Creates a match. Every match needs an invitation code + an initial user
    fun createInviteMatch(userID: Long, channel: MessageChannel): Response {
        if(usersMatches.containsKey(userID)) return Response(Result.HAS_RUNNING_MATCH)

        val inviteLink = Utils.getInviteLink(userID)
        Match().also {
            it.inviteLink = inviteLink
            it.addPlayer(MatchPlayer(it, userID, channel))
            inviteMatches[inviteLink] = it
            usersMatches[userID] = it
            println("Match created: $it")
        }
        return Response(Result.SUCCESS, inviteLink)
    }

    fun getUserMatch(userID: Long): Match? = usersMatches[userID]

    fun getMatch(inviteLink: String): Match? = inviteMatches[inviteLink]

    fun joinMatch(inviteLink: String, userID: Long, channel: MessageChannel): Response {
        if(!inviteMatches.containsKey(inviteLink)) return Response(Result.NOT_FOUND)
        if(usersMatches.containsKey(userID)) return Response(Result.HAS_RUNNING_MATCH)

        inviteMatches[inviteLink]!!.also {
            if(it.isFull()) return Response(Result.MATCH_FULL)
            if(it.isInMatch(userID)) return Response(Result.SELF_JOIN_ERROR)

            it.addPlayer(MatchPlayer(it, userID, channel))
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
package io.wollinger.rataudc

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class MatchPlayer(val userID: Long, val channel: MessageChannel) {
    var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )
}

class Match {
    var inviteLink: String? = null
    var player1: MatchPlayer? = null
    var player2: MatchPlayer? = null
}

object MatchManager {
    private val inviteMatches = HashMap<String, Match>()
    private val usersMatches = HashMap<Long, Match>()

    fun createInviteMatch(userID: Long, channel: MessageChannel): String? {
        if(usersMatches.containsKey(userID)) return null

        val inviteLink = Utils.getInviteLink(userID)
        Match().also {
            it.inviteLink = inviteLink
            it.player1 = MatchPlayer(userID, channel)
            inviteMatches[inviteLink] = it
            usersMatches[userID] = it
        }
        return inviteLink
    }

    fun leave(userID: Long) {
        //TODO: Add events to notify players and match
        usersMatches[userID]?.also {
            usersMatches.remove(userID)
            inviteMatches.remove(it.inviteLink)
        }
    }
}
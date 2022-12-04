package io.wollinger.rataudc

class Match(val ownerID: Long) {
    var opponentID: Long? = null
    val startTime: Long = Utils.currentTime()

    var ownerBoard = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )
    var opponentBoard = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    fun isWaiting() = opponentID == null
}

object MatchManager {
    private val matches = HashMap<Long, Match>()

    fun totalMatches() = matches.size
    fun waitingUsers(): Int {
        var w = 0
        matches.forEach { (_, match) ->
            if(match.isWaiting()) w++
        }
        return w
    }
}
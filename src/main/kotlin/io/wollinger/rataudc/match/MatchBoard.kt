package io.wollinger.rataudc.match

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class MatchBoard {
    var changeListener: ((MatchBoard) -> Unit)? = null

    private var board = arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0)
    )

    fun getPiece(x: Int, y: Int) = board[y][x]
    fun setPiece(x: Int, y: Int, piece: Int, quietChange: Boolean = false) {
        board[y][x] = piece
        if(!quietChange) changeListener?.invoke(this)
    }

    fun hasSpace(column: Int) = getPiece(column, 0) == 0 || getPiece(column, 1) == 0 || getPiece(column, 2) == 0

    //Returns true if something was modified
    fun addPiece(column: Int, roll: Int): Boolean {
        if(roll == 0) return false

        for(y in 0..2) {
            if(getPiece(column, y) == 0) {
                setPiece(column, y, roll)
                return true
            }
        }
        return false
    }

    fun calculateColumnScore(column: Int): Int {
        val numbers = CopyOnWriteArrayList<Int>()
        for(i in 0..2) numbers.add(getPiece(column, i))

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

    fun calculateScore(): Int {
        var totalScore = 0
        for(i in 0..2) totalScore += calculateColumnScore(i)
        return totalScore
    }

    //Returns true if we did destroy something
    fun destroyPieces(column: Int, piece: Int): Boolean {
        if(piece == 0) return false
        var didDestroy = false
        for (i in 0..2) {
            if (getPiece(column, i) == piece) {
                //Quiet change, we don't want this to cause any updates by itself visually
                setPiece(column, i, 0, true)
                didDestroy = true
            }
        }
        return didDestroy
    }
}
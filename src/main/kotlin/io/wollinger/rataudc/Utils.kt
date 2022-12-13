package io.wollinger.rataudc

import org.apache.commons.codec.digest.DigestUtils
import java.awt.*
import java.awt.image.BufferedImage


object Utils {
    private val imgCache = HashMap<String, BufferedImage>()

    fun currentTime() = System.currentTimeMillis()

    fun getInviteLink(id: Long): String = DigestUtils.md5Hex(id.toString() + currentTime().toString())

    private fun findFont(dim: Dimension, oldFont: Font, text: String, g: Graphics): Font {
        var savedFont = oldFont
        for (i in 0..256) {
            val newFont = oldFont.deriveFont(i.toFloat())
            val newDim = Dimension(g.getFontMetrics(newFont).stringWidth(text), newFont.size)
            if (dim.height < newDim.height || dim.width < newDim.width) {
                return savedFont
            }
            savedFont = newFont
        }
        return oldFont
    }

    fun renderStringToImage(string: String, width: Int, height: Int): BufferedImage {
        val id = "${string}_${width}_$height"
        return imgCache[id] ?: BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also {
            if(string.isEmpty()) return@also

            val g = it.graphics as Graphics2D
            g.antialise()
            g.font = findFont(Dimension(width, height), Resources.font, string, g)
            val strWidth = g.fontMetrics.stringWidth(string)
            g.drawString(string, width / 2 - strWidth / 2, height - height / 4)
        }.also {
            imgCache[id] = it
        }
    }

    fun renderDiceWithBG(piece: Int, width: Int, height: Int, type: DiceType): BufferedImage {
        val id = "${piece}_${width}_${height}_${type}"
        return imgCache[id] ?: BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            g.drawImage(Resources.bg, 0, 0, width, height, null)
            if(piece == 0) return@also
            val diceSize = (height * 0.8).toInt()
            fun d(n: Int) = n / 2 - diceSize / 2
            g.drawImage(Resources.dice[piece - 1].getByType(type), d(width), d(height), diceSize, diceSize, null)
        }.also {
            imgCache[id] = it
        }
    }
}
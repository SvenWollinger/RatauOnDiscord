package io.wollinger.rataudc

import org.apache.commons.codec.digest.DigestUtils
import java.awt.*
import java.awt.image.BufferedImage


object Utils {
    fun currentTime() = System.currentTimeMillis()

    fun getInviteLink(id: Long): String = DigestUtils.md5Hex(id.toString() + currentTime().toString())

    fun findFont(dim: Dimension, oldFont: Font, text: String, g: Graphics): Font {
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
        return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also {
            val g = it.graphics as Graphics2D
            g.antialise()
            g.font = findFont(Dimension(width, height), Resources.font, string, g)
            g.drawString(string, 0, height - height / 4)
        }
    }
}
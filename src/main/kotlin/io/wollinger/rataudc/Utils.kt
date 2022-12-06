package io.wollinger.rataudc

import org.apache.commons.codec.digest.DigestUtils
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics


object Utils {
    fun currentTime() = System.currentTimeMillis()

    fun getInviteLink(id: Long): String = DigestUtils.md5Hex(id.toString() + currentTime().toString())

    private fun getFontSize(graphics: Graphics, font: Font, text: String): Dimension {
        graphics.getFontMetrics(font).also { metrics ->
            return Dimension(metrics.stringWidth(text) + 2, metrics.height + 2)
        }
    }

    fun findFont(componentSize: Dimension, oldFont: Font, text: String, g: Graphics): Font {
        var savedFont: Font = oldFont
        for (i in 0..99) {
            val newFont = oldFont.deriveFont(i.toFloat())
            val d: Dimension = getFontSize(g, newFont, text)
            if (componentSize.height < d.height || componentSize.width < d.width) {
                return savedFont
            }
            savedFont = newFont
        }
        return oldFont
    }
}
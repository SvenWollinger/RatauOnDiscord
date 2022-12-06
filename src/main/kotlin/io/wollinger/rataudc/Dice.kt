package io.wollinger.rataudc

import java.awt.Font
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Dice {
    val dice = Array<BufferedImage>(6) { ImageIO.read(Dice::class.java.getResource("/dice${it + 1}.png")) }
    val bg = ImageIO.read(Dice::class.java.getResource("/bg.png"))
    val font = Font.createFont(Font.TRUETYPE_FONT, Dice::class.java.getResourceAsStream("/Eczar SemiBold 600.ttf"))
}
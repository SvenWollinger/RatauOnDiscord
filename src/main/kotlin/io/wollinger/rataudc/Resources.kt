package io.wollinger.rataudc

import java.awt.Font
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Resources {
    val dice = Array<BufferedImage>(6) { ImageIO.read(Resources::class.java.getResource("/dice${it + 1}.png")) }
    val bg = ImageIO.read(Resources::class.java.getResource("/bg.png"))
    val font = Font.createFont(Font.TRUETYPE_FONT, Resources::class.java.getResourceAsStream("/Eczar SemiBold 600.ttf"))
}
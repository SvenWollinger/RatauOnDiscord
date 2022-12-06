package io.wollinger.rataudc

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Dice {
    val dice = Array<BufferedImage>(6) { ImageIO.read(Dice::class.java.getResource("/dice${it + 1}.png")) }
    val bg = ImageIO.read(Dice::class.java.getResource("/bg.png"))
}
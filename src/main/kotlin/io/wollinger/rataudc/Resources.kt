package io.wollinger.rataudc

import java.awt.Font
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

enum class DiceType { PLAIN, YELLOW, BLUE }
data class DiceTexture(val plain: BufferedImage, val yellow: BufferedImage,val blue: BufferedImage) {
    fun getByType(type: DiceType) = when(type) {
        DiceType.PLAIN -> plain
        DiceType.YELLOW -> yellow
        DiceType.BLUE -> blue
    }
}

object Resources {
    val dice = Array(6) {
        val plain = ImageIO.read(Resources::class.java.getResource("/dice${it + 1}.png"))
        val yellow = ImageIO.read(Resources::class.java.getResource("/dice${it + 1}_yellow.png"))
        val blue = ImageIO.read(Resources::class.java.getResource("/dice${it + 1}_blue.png"))
        DiceTexture(plain, yellow, blue)
    }
    val bg: BufferedImage = ImageIO.read(Resources::class.java.getResource("/bg.png"))
    val font: Font = Font.createFont(Font.TRUETYPE_FONT, Resources::class.java.getResourceAsStream("/Eczar SemiBold 600.ttf"))
}
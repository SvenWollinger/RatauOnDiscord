package io.wollinger.rataudc

import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun BufferedImage.toFileUpload(): FileUpload {
    File.createTempFile("ratau-tmp", ".png").also {
        ImageIO.write(this, "png", it)
        return FileUpload.fromData(it)
    }
}

fun Graphics2D.antialise() {
    this.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    this.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
}
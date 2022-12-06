package io.wollinger.rataudc

import net.dv8tion.jda.api.utils.FileUpload
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun BufferedImage.toFileUpload(): FileUpload {
    File.createTempFile("ratau-tmp", ".png").also {
        ImageIO.write(this, "png", it)
        return FileUpload.fromData(it)
    }
}
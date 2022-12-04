package io.wollinger.rataudc

import java.io.File
import java.nio.file.Files

fun main() {
    var token: String
    File("token.txt").also {
        if(it.exists()) {
            token = Files.readAllLines(it.toPath())[0]
        } else {
            print("Enter token: ")
            token = readln()
            Files.write(it.toPath(), token.toByteArray())
        }
    }
    Ratau(token)
}
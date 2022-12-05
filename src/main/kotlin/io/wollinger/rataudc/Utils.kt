package io.wollinger.rataudc

import org.apache.commons.codec.digest.DigestUtils


object Utils {
    fun currentTime() = System.currentTimeMillis()

    fun getInviteLink(id: Long) = DigestUtils.md5Hex(id.toString() + currentTime().toString())
}
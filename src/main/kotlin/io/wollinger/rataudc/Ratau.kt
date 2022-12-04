package io.wollinger.rataudc

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

class Ratau(token: String) {
    private var jda: JDA

    init {
        jda = JDABuilder.createDefault(token).also {
            it.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            it.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES)
            it.setBulkDeleteSplittingEnabled(false)
            it.setActivity(Activity.playing("Knucklebones"))
        }.build()
        jda.awaitReady()
    }
}
package io.wollinger.rataudc

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

class Ratau(token: String) {
    private var jda: JDA

    init {
        //Set up JDA
        jda = JDABuilder.createDefault(token).also {
            it.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            it.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES)
            it.setBulkDeleteSplittingEnabled(false)
            it.setActivity(Activity.playing("Knucklebones"))
        }.build()
        jda.awaitReady()

        //Set up slash commands
        val commands = ArrayList<SlashCommandData>()
        Commands.slash("kb-create", "Create a knucklebones match").also {
            it.isGuildOnly = true
            commands.add(it)
        }
        Commands.slash("kb-join", "Join someones knucklebone match").also {
            it.isGuildOnly = true
            it.addOption(OptionType.USER, "user", "User to join", true)
            commands.add(it)
        }
        Commands.slash("kb-leave", "Leave your current match").also {
            it.isGuildOnly = true
            commands.add(it)
        }
        jda.updateCommands().addCommands(commands).complete()

        //Print registered commands
        jda.retrieveCommands().queue {
            it.forEach { cmd -> println("Registered command: $cmd") }
        }
    }
}
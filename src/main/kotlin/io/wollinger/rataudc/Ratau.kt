package io.wollinger.rataudc

import io.wollinger.rataudc.commands.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

class Ratau(token: String): ListenerAdapter() {
    companion object {
        lateinit var jda: JDA
    }

    private val commands = HashMap<String, ICommand>().also {
        it[KBCreate.label] = KBCreate
        it[KBJoin.label] = KBJoin
        it[KBLeave.label] = KBLeave
        it[KBList.label] = KBList
    }

    init {
        //Set up JDA
        jda = JDABuilder.createDefault(token).also {
            it.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            it.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES)
            it.setBulkDeleteSplittingEnabled(false)
            it.setActivity(Activity.playing("Knucklebones"))
            it.addEventListeners(this)
        }.build()
        jda.awaitReady()

        //Set up slash commands
        ArrayList<CommandData>().also {
            commands.forEach { (_, cmd) ->
                it.add(cmd.getSlashCommand())
                cmd.ratau = this
            }
            jda.updateCommands().addCommands(it).complete()
        }

        //Print registered commands
        jda.retrieveCommands().queue {
            it.forEach { cmd -> println("Registered command: $cmd") }
        }

        MatchManager.startServices()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commands[event.name]?.run(event)
    }
}
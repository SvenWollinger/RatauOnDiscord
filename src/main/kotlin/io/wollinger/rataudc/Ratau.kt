package io.wollinger.rataudc

import io.wollinger.rataudc.commands.*
import io.wollinger.rataudc.match.MatchManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag


object Ratau: ListenerAdapter() {
    lateinit var jda: JDA
    lateinit var token: String
    lateinit var ownerID: String
    lateinit var ownerUsername: String

    private val commands = HashMap<String, ICommand>().also {
        it[Help.label] = Help
        it[KBCreate.label] = KBCreate
        it[KBJoin.label] = KBJoin
        it[KBLeave.label] = KBLeave
    }

    fun init() {
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
            commands.forEach { (_, cmd) -> it.add(cmd.getSlashCommand()) }
            jda.updateCommands().addCommands(it).complete()
        }

        //Print registered commands
        jda.retrieveCommands().queue {
            it.forEach { cmd -> println("Registered command: $cmd") }
        }

        MatchManager.startServices()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        event.author.idLong.also {
            val success = MatchManager.getUserMatch(it)?.handleMessage(it, event.message.contentRaw) ?: false
            if(success) event.message.addReaction(Emoji.fromUnicode("\uD83D\uDCDD")).queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val parts = event.componentId.split("-").toMutableList()
        when(parts.removeAt(0)) {
            "help" -> Help.doHelp(event, parts[0])
            "kb" -> {
                event.respondEmpty()
                val buttonOwner = parts[1].toLong()
                if(buttonOwner != event.user.idLong) return
                MatchManager.getMatch(parts[0])?.buttonEvent(buttonOwner, parts[2])
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commands[event.name]?.run(event)
    }
}
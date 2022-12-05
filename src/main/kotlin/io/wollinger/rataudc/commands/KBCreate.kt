package io.wollinger.rataudc.commands

import io.wollinger.rataudc.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBCreate: ICommand {
    override val label = "kb-create"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {
        val link = MatchManager.createInviteMatch(event.user.idLong, event.channel)
        val message = if(link == null) "You already have a running match!" else "Match created!\nInvite link: $link"
        event.reply(message).queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Create a knucklebones match")
    }
}
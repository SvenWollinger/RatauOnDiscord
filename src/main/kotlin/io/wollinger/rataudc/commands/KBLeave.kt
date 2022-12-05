package io.wollinger.rataudc.commands

import io.wollinger.rataudc.MatchManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBLeave: ICommand {
    override val label = "kb-leave"

    override fun run(event: SlashCommandInteractionEvent) {
        val response = MatchManager.leave(event.user.idLong)
        val message = when(response.result) {
            MatchManager.Result.SUCCESS -> "Match left"
            MatchManager.Result.NOT_FOUND -> "Not in an active match"
            else -> "Bad response: ${response.result}"
        }
        event.reply(message).queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Leave your current match")
    }
}
package io.wollinger.rataudc.commands

import io.wollinger.rataudc.match.MatchManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

object KBLeave: ICommand {
    override val label = "kb-leave"

    override fun run(event: SlashCommandInteractionEvent) {
        val response = MatchManager.leave(event.user.idLong)
        val message = when(response.result) {
            MatchManager.Result.SUCCESS -> "Match left"
            MatchManager.Result.NOT_FOUND -> "Not in an active match"
            else -> "Bad response: ${response.result}"
        }
        event.reply(message).setEphemeral(true).queue()
    }

    override fun getSlashCommand() = Commands.slash(label, "Leave your current match")
}
package io.wollinger.rataudc.commands

import io.wollinger.rataudc.match.MatchManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBCreate: ICommand {
    override val label = "kb-create"

    override fun run(event: SlashCommandInteractionEvent) {
        val response = MatchManager.createInviteMatch(event.user.idLong, event.channel)
        val message = when(response.result) {
            MatchManager.Result.SUCCESS -> "Match created!\nInvite link: ${response.content}"
            MatchManager.Result.HAS_RUNNING_MATCH -> "You already have a running match!"
            else -> "Bad response: ${response.result}"
        }
        event.reply(message).setEphemeral(true).queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Create a knucklebones match")
    }
}
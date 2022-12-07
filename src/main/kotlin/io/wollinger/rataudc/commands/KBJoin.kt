package io.wollinger.rataudc.commands

import io.wollinger.rataudc.MatchManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBJoin: ICommand {
    override val label = "kb-join"

    override fun run(event: SlashCommandInteractionEvent) {
        val inviteLink = event.options[0].asString
        event.deferReply().queue()
        val response = MatchManager.joinMatch(inviteLink, event.user.idLong, event.channel)
        val message = when(response.result) {
            MatchManager.Result.NOT_FOUND -> "Match not found!"
            MatchManager.Result.SELF_JOIN_ERROR -> "You cant join yourself!"
            MatchManager.Result.SUCCESS -> "Joined game!"
            else -> "Bad response: ${response.result}"
        }
        event.hook.editOriginal(message).queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Join someones knucklebone match").also {
            it.addOption(OptionType.STRING, "invite-link", "Invite link", true)
        }
    }
}
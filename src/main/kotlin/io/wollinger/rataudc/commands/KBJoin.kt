package io.wollinger.rataudc.commands

import io.wollinger.rataudc.match.MatchManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object KBJoin: ICommand {
    override val label = "kb-join"

    override fun run(event: SlashCommandInteractionEvent) {
        val inviteLink = event.options[0].asString
        event.deferReply().setEphemeral(true).queue()
        val response = MatchManager.joinMatch(inviteLink, event.user.idLong, event.channel)
        val message = when(response.result) {
            MatchManager.Result.NOT_FOUND -> "Match not found!"
            MatchManager.Result.SELF_JOIN_ERROR -> "You cant join yourself!"
            MatchManager.Result.SUCCESS -> "Joined game!"
            MatchManager.Result.HAS_RUNNING_MATCH -> "You are already in a running match!"
            MatchManager.Result.MATCH_FULL -> "Match is full!"
        }
        event.hook.editOriginal(message).queue()
    }

    override fun getSlashCommand() =  Commands.slash(label, "Join someones knucklebone match").also {
        it.addOption(OptionType.STRING, "invite-link", "Invite link", true)
    }
}
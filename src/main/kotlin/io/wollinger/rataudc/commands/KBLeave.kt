package io.wollinger.rataudc.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBLeave: ICommand {
    override val label = "kb-leave"

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("leave").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash("kb-leave", "Leave your current match").also {
            it.isGuildOnly = true
        }
    }
}
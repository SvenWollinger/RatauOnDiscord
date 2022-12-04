package io.wollinger.rataudc.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBCreate: ICommand {
    override val label = "kb-create"

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("create").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash("kb-create", "Create a knucklebones match").also {
            it.isGuildOnly = true
        }
    }
}
package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBCreate: ICommand {
    override val label = "kb-create"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("create").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Create a knucklebones match")
    }
}
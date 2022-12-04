package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBLeave: ICommand {
    override val label = "kb-leave"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("leave").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Leave your current match")
    }
}
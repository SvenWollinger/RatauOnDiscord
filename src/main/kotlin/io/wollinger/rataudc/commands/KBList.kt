package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBList: ICommand {
    override val label = "kb-list"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {

    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "List current matches")
    }
}
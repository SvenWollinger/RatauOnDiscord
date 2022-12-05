package io.wollinger.rataudc.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBList: ICommand {
    override val label = "kb-list"

    override fun run(event: SlashCommandInteractionEvent) {

    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "List current matches")
    }
}
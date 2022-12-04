package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {
    val label: String
    var ratau: Ratau
    fun run(event: SlashCommandInteractionEvent)
    fun getSlashCommand(): SlashCommandData
}
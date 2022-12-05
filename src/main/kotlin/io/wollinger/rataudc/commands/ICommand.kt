package io.wollinger.rataudc.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {
    val label: String
    fun run(event: SlashCommandInteractionEvent)
    fun getSlashCommand(): SlashCommandData
}
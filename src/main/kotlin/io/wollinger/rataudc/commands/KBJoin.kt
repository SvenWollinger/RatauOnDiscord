package io.wollinger.rataudc.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBJoin: ICommand {
    override val label = "kb-join"

    override fun run(event: SlashCommandInteractionEvent) {
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash("kb-join", "Join someones knucklebone match").also {
            it.isGuildOnly = true
            it.addOption(OptionType.USER, "user", "User to join", true)
        }
    }
}
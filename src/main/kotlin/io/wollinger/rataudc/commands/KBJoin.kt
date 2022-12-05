package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBJoin: ICommand {
    override val label = "kb-join"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("join").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "Join someones knucklebone match").also {
            it.addOption(OptionType.STRING, "invite-link", "Invite link")
        }
    }
}
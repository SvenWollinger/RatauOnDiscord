package io.wollinger.rataudc.commands

import io.wollinger.rataudc.MatchManager
import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object KBList: ICommand {
    override val label = "kb-list"
    override lateinit var ratau: Ratau

    override fun run(event: SlashCommandInteractionEvent) {
        val waiting = MatchManager.waitingUsers()
        val total = MatchManager.totalMatches()
        event.reply("$waiting Players waiting. $total total current matches.").queue()
    }

    override fun getSlashCommand(): SlashCommandData {
        return Commands.slash(label, "List current matches")
    }
}
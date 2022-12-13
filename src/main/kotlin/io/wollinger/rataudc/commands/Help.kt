package io.wollinger.rataudc.commands

import io.wollinger.rataudc.Ratau
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button

object Help: ICommand {
    override val label = "help"

    override fun run(event: SlashCommandInteractionEvent) {
        event.reply("You need help ey? Pick a topic here...").addActionRow(
            Button.primary("help-about", "Who are you?"),
            Button.primary("help-kb", "Knucklebones"),
            Button.primary("help-contact", "Contact")
        ).setEphemeral(true).queue()
    }

    fun doHelp(event: ButtonInteractionEvent, id: String) {
        when(id) {
            "about" -> {
                val message = """
                    I am Ratau!
                    ... or rather a bot made to mimic him!
                    With me you can play Knucklebones! That's all really.. at least for now.
                    
                    You can find my inner workings here: <https://github.com/SvenWollinger/RatauOnDiscord>
                """.trimIndent()
                event.reply(message).setEphemeral(true).queue()
            }
            "kb" -> {
                event.reply("TODO: Edit this").setEphemeral(true).queue()
            }
            "contact" -> {
                val message = """
                    I am being developed by Sven Wollinger, you can find his website here: <https://wollinger.io>
                    My Git Repository is here: <https://github.com/SvenWollinger/RatauOnDiscord>
                    
                    This bot is being hosted by this fella, please redirect all concerns/questions regarding this hosted instance to them:
                    ${Ratau.ownerUsername} (<@${Ratau.ownerID}>)
                """.trimIndent()
                event.reply(message).setEphemeral(true).queue()
            }
            else -> {
                event.reply("Bad id: $id. Report this to the dev. Thanks!").setEphemeral(true).queue()
            }
        }
    }

    override fun getSlashCommand() = Commands.slash(label, "Fear not! I am Ratau. I was sent to guide you.")
}
package ru.idfedorov09.telegram.bot.base.domain

import ru.idfedorov09.telegram.bot.base.config.registry.TextCommand

object BroadcastCommands {
    val CREATE_BROADCAST_COMMAND = TextCommand(
        command = "/create_broadcast",
        description = "Создает рассылку",
        showInHelp = true,
    )
}
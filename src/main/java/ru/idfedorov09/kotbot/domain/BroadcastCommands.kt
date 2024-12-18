package ru.idfedorov09.telegram.bot.base.domain

import ru.idfedorov09.telegram.bot.base.config.registry.TextCommand

object BroadcastCommands {
    val CREATE_BROADCAST_COMMAND = TextCommand(
        command = "/create_broadcast",
        // TODO: allowed roles
        description = "Создает рассылку",
        showInHelp = true,
    )
    val CATEGORY_COMMAND = TextCommand(
        command = "/manage_categories",
        // TODO: allowed roles
        description = "Управление категориями",
        showInHelp = true,
    )
}
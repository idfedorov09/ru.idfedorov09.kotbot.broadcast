package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.config.registry.LastUserActionType

object BroadcastLastUserActionType {
    val CREATE_BROADCAST_DEFAULT = LastUserActionType(
        "CREATE_BROADCAST_DEFAULT",
        "Создание бродкаста"
    )
}
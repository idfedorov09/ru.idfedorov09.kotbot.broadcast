package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.config.registry.LastUserActionType

object BroadcastLastUserActionType {
    val ENTRY_CREATE_POST = LastUserActionType(
        "ENTRY_CREATE_POST",
        "Создание поста - точка входа"
    )

    val DEFAULT_CREATE_POST = LastUserActionType(
        "DEFAULT_CREATE_POST",
        "Дефолтный стейт создания поста (меню создания)"
    )
}
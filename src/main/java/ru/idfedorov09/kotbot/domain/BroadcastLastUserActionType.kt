package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.config.registry.LastUserActionType

object BroadcastLastUserActionType {
    val ENTRY_CREATE_POST = LastUserActionType(
        type = "ENTRY_CREATE_POST",
        description = "Создание поста - точка входа",
    )
    val DEFAULT_CREATE_POST = LastUserActionType(
        type = "DEFAULT_CREATE_POST",
        description = "Дефолтный стейт создания поста (меню создания)",
    )
    val PC_TEXT_TYPE = LastUserActionType(
        type = "PC_TEXT_TYPE",
        description = "Ввод текста поста",
    )
    val PC_PHOTO_TYPE = LastUserActionType(
        type = "PC_PHOTO_TYPE",
        description = "Ввод фото поста",
    )
    val PC_BUTTON_CAPTION_TYPE = LastUserActionType(
        type = "PC_BUTTON_CAPTION_TYPE",
        description = "Ввод текста кнопки",
    )
    val PC_BUTTON_LINK_TYPE = LastUserActionType(
        type = "PC_BUTTON_LINK_TYPE",
        description = "Ввод ссылки кнопки",
    )
    val PC_BUTTON_CALLBACK_TYPE = LastUserActionType(
        type = "PC_BUTTON_CALLBACK_TYPE",
        description = "Ввод текста коллбэка кнопки"
    )
    val PC_NAME_TYPE = LastUserActionType(
        type = "PC_NAME_TYPE",
        description = "Ввод названия поста"
    )

    val CAT_NAME_TYPE = LastUserActionType(
        type = "CAT_NAME_TYPE",
        description = "Ввод названия категории"
    )
    val CAT_DESCRIPTION_TYPE = LastUserActionType(
        type = "CAT_DESCRIPTION_TYPE",
        description = "Ввод описания категории"
    )
}
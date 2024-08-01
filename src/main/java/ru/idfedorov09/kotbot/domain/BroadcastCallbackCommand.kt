package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.config.registry.CallbackCommand

object BroadcastCallbackCommand {
    val POST_CHANGE_TEXT = CallbackCommand(
        command = "post_change_text",
        description = "Изменение текста поста",
    )
    val POST_CHANGE_PHOTO = CallbackCommand(
        command = "post_change_photo",
        description = "Изменение фото поста",
    )
    val POST_DELETE_PHOTO = CallbackCommand(
        command = "post_delete_photo",
        description = "Удаление фото из поста",
    )
    val POST_CREATE_CANCEL = CallbackCommand(
        command = "post_create_cancel",
        description = "Отмена создания поста",
    )
    val POST_ACTION_CANCEL = CallbackCommand(
        command = "post_action_cancel",
        description = "Отмена действия при создании поста (например, отмена изменения текста)"
    )
    val POST_PREVIEW = CallbackCommand(
        command = "post_preview",
        description = "Предпросмотр поста",
    )
    val POST_ADD_BUTTON = CallbackCommand(
        command = "post_add_button",
        description = "Добавление кнопки к посту",
    )
    val POST_BUTTON_SETTINGS_CONSOLE = CallbackCommand(
        command = "post_change_button_console",
        description = "Консоль настройки кнопки",
    )
    val POST_CHANGE_BUTTON = CallbackCommand(
        command = "post_change_button",
        description = "Изменение конкретной кнопки (id в user data)",
    )
    val POST_CHANGE_BUTTON_CAPTION = CallbackCommand(
        command = "post_change_button_caption",
        description = "Изменение текста на кнопке",
    )
    val POST_CHANGE_BUTTON_LINK = CallbackCommand(
        command = "post_change_button_link",
        description = "Изменение ссылки кнопки",
    )
    val POST_CHANGE_BUTTON_CALLBACK = CallbackCommand(
        command = "post_change_button_callback",
        description = "Изменение коллбэка кнопки",
    )
    val POST_DELETE_BUTTON = CallbackCommand(
        command = "post_delete_button",
        description = "Удаление кнопки",
    )
    val POST_TOGGLE_PREVIEW = CallbackCommand(
        command = "post_toggle_preview",
        description = "Изменение флага превью",
    )

    val BROADCAST_SELECT_POST = CallbackCommand(
        command = "bc_select_existing_post",
        description = "Выбор существующего поста",
    )
    val BROADCAST_CREATE_NEW_POST = CallbackCommand(
        command = "bc_create_new_post",
        description = "Создать новый пост",
    )
    val BROADCAST_CREATE_CANCEL = CallbackCommand(
        command = "bc_create_cancel",
        description = "Отмена создания рассылки",
    )
    val BROADCAST_SEND_NOW = CallbackCommand(
        command = "bc_send_post_now",
        description = "Отправить рассылку сейчас",
    )
    val BROADCAST_SCHEDULE_SEND = CallbackCommand(
        command = "bc_schedule_send",
        description = "Отложенная рассылка"
    )
    val POST_CHANGE_NAME = CallbackCommand(
        command = "post_change_name",
        description = "Ввести/изменить название поста (используется также при Сохранить и выйти)",
    )
    val POST_TRY_TO_CLOSE_WITH_SAVE = CallbackCommand(
        command = "pc_post_try_to_close_with_save",
        description = "Попытка выхода из меню редактирования с сохранением поста",
    )
}
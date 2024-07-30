package ru.idfedorov09.kotbot.domain

import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_SCHEDULE_SEND
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_SEND_NOW
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.POST_BROADCAST_SAVE_POST_AND_EXIT
import ru.idfedorov09.kotbot.fetcher.PostConstructorFetcher.Companion.POST_ACTION_CANCEL
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

// TODO: roles? ??
object PostClassifiers {
    val createNewPost = PostClassifier(
        type = "createPostClassifier",
        createKeyboardAction = ::createNewPostKeyboard
    )
    val choosePost = PostClassifier(
        type = "choosePostClassifier",
        createKeyboardAction = ::createChoosePostKeyboard
    )

    private fun createChoosePostKeyboard(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO,
    ): List<List<CallbackDataDTO>> {
        val sendNow = CallbackDataDTO(
            callbackData = BROADCAST_SEND_NOW,
            metaText = "Разослать сейчас",
        )
        val scheduleSending = CallbackDataDTO(
            callbackData = BROADCAST_SCHEDULE_SEND,
            metaText = "Запланировать рассылку",
        )
        val editPost = CallbackDataDTO(
            // TODO: тут проблемка рисуется
            // В контекст пост кладется из поиска по автору
            // имеет смысл избавиться от этой логики и передавать id поста везде в коллбэках (параметром)
            metaText = "Редактировать пост (TODO)"
        )
        return listOf(sendNow, scheduleSending, editPost).map { listOf(it) }
    }

    private fun createNewPostKeyboard(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO,
    ): List<List<CallbackDataDTO>> {
        val sendNow = CallbackDataDTO(
            callbackData = BROADCAST_SEND_NOW,
            metaText = "Разослать сейчас",
        )
        val scheduleSending = CallbackDataDTO(
            callbackData = BROADCAST_SCHEDULE_SEND,
            metaText = "Запланировать рассылку",
        )
        val justSaveAndExit = CallbackDataDTO(
            callbackData = POST_BROADCAST_SAVE_POST_AND_EXIT,
            metaText = "Сохранить и выйти",
        )
        val backToPc = CallbackDataDTO(
            callbackData = POST_ACTION_CANCEL,
            metaText = "Назад к конструктору",
        )
        return listOf(sendNow, scheduleSending, justSaveAndExit, backToPc).map { listOf(it) }
    }
}
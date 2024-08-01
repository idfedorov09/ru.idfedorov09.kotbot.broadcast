package ru.idfedorov09.kotbot.domain

import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.kotbot.domain.GlobalConstants.setClassifier
import ru.idfedorov09.kotbot.domain.GlobalConstants.setPostId
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_CREATE_NEW_POST
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_SCHEDULE_SEND
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_SEND_NOW
import ru.idfedorov09.kotbot.fetcher.PostConstructorFetcher.Companion.POST_ACTION_CANCEL
import ru.idfedorov09.kotbot.fetcher.PostConstructorFetcher.Companion.POST_TRY_TO_CLOSE_WITH_SAVE
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

// TODO: roles? ??
object PostClassifiers {
    val lastBroadcastStepClassifier = PostClassifier(
        type = "lastBroadcastStepClassifier",
        createKeyboardAction = ::lastBroadcastStep
    )
    val defaultSaveClassifier = PostClassifier(
        type = "defaultBroadcastClassifier",
        createKeyboardAction = ::defaultBroadcastClassifier
    )

    private fun defaultBroadcastClassifier(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO?,
    ): List<List<CallbackDataDTO>> {
        val backToPc = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_NEW_POST,
            metaText = "Назад к конструктору",
        ).setPostId(postId = post.id!!)
        return listOf(listOf(backToPc))
    }

    private fun lastBroadcastStep(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO?,
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
            callbackData = POST_TRY_TO_CLOSE_WITH_SAVE,
            metaText = "Сохранить и выйти",
        ).setClassifier(lastBroadcastStepClassifier).setPostId(post.id!!)
        val backToPc = CallbackDataDTO(
            callbackData = POST_ACTION_CANCEL,
            metaText = "Назад к конструктору",
        )
        return listOf(sendNow, scheduleSending, justSaveAndExit, backToPc).map { listOf(it) }
    }
}
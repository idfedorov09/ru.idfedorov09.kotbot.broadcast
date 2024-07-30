package ru.idfedorov09.kotbot.domain

import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

object PostClassifiers {
    val broadcastClassifier = PostClassifier(
        type = "broadcastClassifier",
        createKeyboardAction = ::createBroadcastKeyboard
    )

    private fun createBroadcastKeyboard(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO,
    ): List<List<CallbackDataDTO>> {
        // TODO
        return listOf()
    }
}
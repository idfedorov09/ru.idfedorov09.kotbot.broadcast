package ru.idfedorov09.kotbot.config.registry

import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.telegram.bot.base.config.registry.RegistryModel
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

data class PostClassifier(
    val type: String,
    val createKeyboardAction: (
        Update,
        PostDTO,
        UserDTO,
        CallbackDataDTO?,
    ) -> List<List<CallbackDataDTO>>,
): RegistryModel(PostClassifier::class, type) {
    init { registerModel() }
}
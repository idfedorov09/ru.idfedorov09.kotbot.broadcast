package ru.idfedorov09.kotbot.domain.dto

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.entity.PostButtonEntity
import ru.idfedorov09.telegram.bot.base.domain.dto.BaseDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import java.time.LocalDateTime
import java.time.ZoneId

data class PostButtonDTO(
    val id: Long? = null,
    var text: String? = null,
    var link: String? = null,
    var callbackData: String? = null, // TODO: link to CallbackEntity!!
    val author: UserDTO? = null,
    var isDeleted: Boolean = false,
    var lastModifyTime: LocalDateTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
): BaseDTO<PostButtonEntity>() {
    fun createKeyboard() = InlineKeyboardButton().also {
        it.text = text!!
        it.callbackData = callbackData
        it.url = link
    }

    override fun toEntity() = PostButtonEntity(
        id = id,
        text = text,
        link = link,
        callbackData = callbackData,
        author = author?.toEntity(),
        lastModifyTime = lastModifyTime,
        isDeleted = isDeleted,
    )
}
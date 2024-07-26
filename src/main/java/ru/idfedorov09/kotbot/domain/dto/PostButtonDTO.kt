package ru.idfedorov09.kotbot.domain.dto

import ru.idfedorov09.kotbot.domain.entity.PostButtonEntity
import ru.idfedorov09.telegram.bot.base.domain.dto.BaseDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import java.time.LocalDateTime
import java.time.ZoneId

data class PostButtonDTO(
    val id: Long? = null,
    val text: String? = null,
    val link: String? = null,
    val callbackData: String? = null,
    val author: UserDTO? = null,
    val lastModifyTime: LocalDateTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
): BaseDTO<PostButtonEntity>() {
    override fun toEntity() = PostButtonEntity(
        id = id,
        text = text,
        link = link,
        callbackData = callbackData,
        author = author?.toEntity(),
        lastModifyTime = lastModifyTime,
    )
}
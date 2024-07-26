package ru.idfedorov09.kotbot.domain.dto

import ru.idfedorov09.kotbot.domain.entity.PostEntity
import ru.idfedorov09.telegram.bot.base.domain.dto.BaseDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

data class PostDTO(
    val id: Long? = null,
    val text: String? = null,
    val imageHash: String? = null,
    val author: UserDTO,
    val name: String? = null,
    val isBuilt: Boolean = false,
    val lastConsoleMessageId: Int? = null,
    val isDeleted: Boolean = false,
    val shouldShowWebPreview: Boolean = false,
    val buttons: MutableList<PostButtonDTO> = mutableListOf(),
) : BaseDTO<PostEntity>() {
    override fun toEntity() = PostEntity(
        id = id,
        text = text,
        imageHash = imageHash,
        author = author.toEntity(),
        name = name,
        isBuilt = isBuilt,
        lastConsoleMessageId = lastConsoleMessageId,
        isDeleted = isDeleted,
        shouldShowWebPreview = shouldShowWebPreview,
        buttons = buttons.map { it.toEntity() }.toMutableList(),
    )
}
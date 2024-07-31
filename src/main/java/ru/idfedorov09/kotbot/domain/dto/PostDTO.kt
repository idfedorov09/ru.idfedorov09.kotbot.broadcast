package ru.idfedorov09.kotbot.domain.dto

import ru.idfedorov09.kotbot.domain.entity.PostEntity
import ru.idfedorov09.kotbot.domain.service.PostService
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
    val classifier: String? = null,
    val buttons: MutableList<PostButtonDTO> = mutableListOf(),
) : BaseDTO<PostEntity>() {

    companion object {
        private lateinit var postService: PostService
        fun init(service: PostService) {
            postService = service
        }
    }

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
        classifier = classifier,
        buttons = buttons.map { it.toEntity() }.toMutableList(),
    )

    fun save() = postService.save(this)
    fun getLastModifiedButton() = buttons.maxBy { it.lastModifyTime }
}
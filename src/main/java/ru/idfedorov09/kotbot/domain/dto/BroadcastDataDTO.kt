package ru.idfedorov09.kotbot.domain.dto

import ru.idfedorov09.kotbot.domain.entity.BroadcastDataEntity
import ru.idfedorov09.kotbot.domain.service.BroadcastDataService
import ru.idfedorov09.telegram.bot.base.domain.dto.BaseDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

data class BroadcastDataDTO(
    var id: Long?,
    var user: UserDTO,
    var currentPost: PostDTO? = null
): BaseDTO<BroadcastDataEntity>() {

    companion object {
        private lateinit var broadcastDataService: BroadcastDataService
        fun init(service: BroadcastDataService) {
            broadcastDataService = service
        }
    }

    constructor(
        user: UserDTO,
        currentPost: PostDTO? = null,
    ) : this(
        id = user.id,
        user = user,
        currentPost = currentPost,
    )

    override fun toEntity() = BroadcastDataEntity(
        id = id,
        user = user.toEntity(),
        currentPost = currentPost?.toEntity()
    )

    fun save() = broadcastDataService.save(this)
}
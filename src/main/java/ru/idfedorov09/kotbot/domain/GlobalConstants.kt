package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO

object GlobalConstants {
    const val postIdParam = "post_id"
    const val buttonId = "buttonId"

    fun CallbackDataDTO.setPostId(postId: Long) = addParameters(postIdParam to postId)
    fun CallbackDataDTO.getButtonIdParam() = getParams().get(buttonId)
    fun CallbackDataDTO.setButtonIdParam(id: Long) = setParameters(buttonId to id)
}
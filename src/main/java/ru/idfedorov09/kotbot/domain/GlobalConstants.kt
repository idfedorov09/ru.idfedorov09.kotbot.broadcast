package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO

object GlobalConstants {
    const val POST_ID = "post_id"
    const val BUTTON_ID = "button_d"
    const val CURRENT_PAGE = "current_page"
    const val POSTS_PAGE_SIZE = 3 // TODO: 10

    fun CallbackDataDTO.setPostId(postId: Long) = addParameters(POST_ID to postId)
    fun CallbackDataDTO.setButtonIdParam(id: Long) = setParameters(BUTTON_ID to id)
    fun CallbackDataDTO.setCurrentPage(page: Int) = addParameters(CURRENT_PAGE to page)

    fun CallbackDataDTO.getPostId() = getParams().get(POST_ID)
    fun CallbackDataDTO.getButtonIdParam() = getParams().get(BUTTON_ID)
    fun CallbackDataDTO.getCurrentPage() = getParams().get(CURRENT_PAGE)
}
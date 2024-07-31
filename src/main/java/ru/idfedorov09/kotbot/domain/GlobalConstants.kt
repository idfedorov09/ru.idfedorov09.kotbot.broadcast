package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO

object GlobalConstants {
    const val POST_ID = "post_id"
    const val BUTTON_ID = "button_d"
    const val CURRENT_PAGE = "current_page"
    const val POSTS_PAGE_SIZE = 3 // TODO: 10

    private fun CallbackDataDTO.setSomething(key: String, value: Any) =
        callbackData.addParameters(this, key to value)

    fun CallbackDataDTO.setPostId(postId: Long) = setSomething(POST_ID, postId)
    fun CallbackDataDTO.setButtonIdParam(id: Long) = setSomething(BUTTON_ID, id)
    fun CallbackDataDTO.setCurrentPage(page: Int) = setSomething(CURRENT_PAGE, page)

    fun CallbackDataDTO.getPostId() = callbackData.getParams().get(POST_ID)
    fun CallbackDataDTO.getButtonIdParam() = callbackData.getParams().get(BUTTON_ID)
    fun CallbackDataDTO.getCurrentPage() = callbackData.getParams().get(CURRENT_PAGE)
}
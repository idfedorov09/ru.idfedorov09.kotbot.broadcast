package ru.idfedorov09.kotbot.domain

import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.telegram.bot.base.config.registry.LastUserActionType
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO

object GlobalConstants {
    const val POST_ID = "post_id"
    const val BUTTON_ID = "button_d"
    const val CURRENT_PAGE = "current_page"
    const val CLASSIFIER = "classifier"
    const val CATEGORY_ID = "category_id"
    const val TARGET_MESSAGE_ID = "target_message_id"
    const val CATEGORY_TITLE = "category_title"
    const val POSTS_PAGE_SIZE = 3 // TODO: 10
    const val CATEGORIES_PAGE_SIZE = 10

    fun CallbackDataDTO.setParam(key: String, value: Any) =
        callbackData.addParameters(this, key to value)
    fun LastUserActionType.setParam(key: String, value: Any) =
        type.addParameters(this, key to value)

    fun CallbackDataDTO.setPostId(postId: Long) = setParam(POST_ID, postId)
    fun CallbackDataDTO.setCategoryId(categoryId: Long) = setParam(CATEGORY_ID, categoryId)
    fun CallbackDataDTO.setButtonIdParam(id: Long) = setParam(BUTTON_ID, id)
    fun CallbackDataDTO.setCurrentPage(page: Int) = setParam(CURRENT_PAGE, page)
    fun CallbackDataDTO.setClassifier(classifier: PostClassifier) = setParam(CLASSIFIER, classifier.type)
    fun LastUserActionType.setClassifier(classifier: PostClassifier) = setParam(CLASSIFIER, classifier.type)


    fun CallbackDataDTO.getPostId() = callbackData.getParams().get(POST_ID)
    fun CallbackDataDTO.getCategoryId() = callbackData.getParams().get(CATEGORY_ID)
    fun CallbackDataDTO.getButtonIdParam() = callbackData.getParams().get(BUTTON_ID)
    fun CallbackDataDTO.getCurrentPage() = callbackData.getParams().get(CURRENT_PAGE)
    fun CallbackDataDTO.getClassifier() = callbackData.getParams().get(CLASSIFIER)
    fun LastUserActionType.getClassifier() = type.getParams().get(CLASSIFIER)
}
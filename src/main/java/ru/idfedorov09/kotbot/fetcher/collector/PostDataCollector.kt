package ru.idfedorov09.kotbot.fetcher.collector

import org.springframework.stereotype.Component
import ru.idfedorov09.kotbot.domain.GlobalConstants.getPostId
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.BroadcastDataService
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.mephi.sno.libs.flow.belly.InjectData

@Component
class PostDataCollector(
    private val postService: PostService,
    private val broadcastDataService: BroadcastDataService,
) : DefaultFetcher() {

    @InjectData
    fun doFetch(
        user: UserDTO?,
        callbackDataDTO: CallbackDataDTO?,
        broadcastDataDTO: BroadcastDataDTO?,
    ): PostDTO? {
        val userId = user?.id ?: return null
        val broadcastData = broadcastDataDTO
            ?: broadcastDataService.getBroadcastDataByUserId(user.id!!)
            ?: BroadcastDataDTO(user = user).save()
        addToContext(broadcastData)
        if (broadcastData.currentPost != null) return broadcastData.currentPost
        val post = callbackDataDTO
            ?.getPostId()
            ?.toLongOrNull()
            ?.let { postService.findAvailablePostById(it) }
            ?: postService.findCurrentPostByAuthorId(userId)
        addToContext(
            broadcastData.copy(currentPost = post).save()
        )
        return post
    }
}
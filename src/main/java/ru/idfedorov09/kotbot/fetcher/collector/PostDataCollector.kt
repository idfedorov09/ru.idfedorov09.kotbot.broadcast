package ru.idfedorov09.kotbot.fetcher.collector

import org.springframework.stereotype.Component
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.mephi.sno.libs.flow.belly.InjectData

@Component
class PostDataCollector(
    private val postService: PostService,
) : DefaultFetcher() {

    @InjectData
    fun doFetch(user: UserDTO?): PostDTO? {
        val userId = user?.id ?: return null
        return postService.findCurrentPostByAuthorId(userId)
    }
}
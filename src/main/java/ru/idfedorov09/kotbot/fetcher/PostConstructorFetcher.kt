package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import ru.idfedorov09.telegram.bot.base.util.UpdatesUtil
import ru.mephi.sno.libs.flow.belly.InjectData

/**
 * Фетчер, отвечающий за создание поста
 * PC - Post Constructor
 */
@Component
class PostConstructorFetcher(
    private val postService: PostService,
    private val messageSenderService: MessageSenderService,
    private val updatesUtil: UpdatesUtil,
): DefaultFetcher() {

    @InjectData
    private fun doFetch() {}

    @Callback("post_create_cancel")
    fun pcCancel(update: Update, post: PostDTO): PostDTO? {
        deletePcConsole(
            chatId = updatesUtil.getChatId(update),
            post,
        )
        postService.deletePost(post)
        return null
    }

    fun deletePcConsole(chatId: String?, post: PostDTO) {
        chatId ?: return
        post.lastConsoleMessageId ?: return
        messageSenderService.deleteMessage(
            MessageParams(
                chatId = chatId,
                messageId = post.lastConsoleMessageId,
            )
        )
    }

}
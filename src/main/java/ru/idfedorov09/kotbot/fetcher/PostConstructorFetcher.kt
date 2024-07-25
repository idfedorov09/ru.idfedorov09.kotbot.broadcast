package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.CallbackDataService
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
    private val callbackDataService: CallbackDataService,
    private val updatesUtil: UpdatesUtil,
): DefaultFetcher() {

    private companion object {
        const val POST_CREATE_CANCEL = "post_create_cancel"
        const val POST_ACTION_CANCEL = "post_action_cancel"
        const val POST_CHANGE_TEXT = "post_change_text"
    }

    @InjectData
    private fun doFetch() {}

    @Callback(POST_CREATE_CANCEL)
    fun pcCancel(update: Update, post: PostDTO) {
        deletePcConsole(update, post)
        postService.deletePost(post)
    }

    @Callback(POST_CHANGE_TEXT)
    fun pcChangeText(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        deletePcConsole(update, post)
        val msgText =
            "*Напишите текст уведомления*\\.\n\nПравила оформления:\n" +
                    "<b\\>текст</b\\> \\- жирный текст\n" +
                    "<i\\>текст</i\\> \\- выделение курсивом\n" +
                    "<u\\>текст</u\\> \\- подчеркнутый текст\n" +
                    "<s\\>текст</s\\> \\- зачеркнутый текст\n" +
                    "<code\\>текст</code\\> \\- выделенный текст \\(с копированием по клику\\)\n" +
                    "<pre language\\=\"c\\+\\+\"\\>текст</pre\\> \\- исходный код или любой другой текст\n" +
                    "<a href\\='https://sno\\.mephi\\.ru/'\\>Сайт СНО</a\\> \\- ссылка"
        val cancelButton = CallbackDataDTO(
            callbackData = POST_ACTION_CANCEL,
            metaText = "Отмена"
        ).save()

        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = msgText,
                    parseMode = ParseMode.MARKDOWNV2,
                    replyMarkup =
                    createKeyboard(
                        listOf(
                            listOf(
                                cancelButton.createKeyboard()
                            ),
                        ),
                    ),
                ),
            )

        user.lastUserActionType = TODO("BC_TEXT_TYPE")
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    fun deletePcConsole(update: Update, post: PostDTO) =
        deletePcConsole(
            chatId = updatesUtil.getChatId(update),
            post,
        )

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

    // TODO: show console

    private fun CallbackDataDTO.save() = callbackDataService.save(this)!!
    private fun PostDTO.save() = postService.save(this)

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) = InlineKeyboardMarkup().also { it.keyboard = keyboard }

}
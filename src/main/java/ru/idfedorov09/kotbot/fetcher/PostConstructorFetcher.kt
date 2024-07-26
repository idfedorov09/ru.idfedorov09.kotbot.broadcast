package ru.idfedorov09.kotbot.fetcher

import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.DEFAULT_CREATE_POST
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_BUTTON_CAPTION_TYPE
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_BUTTON_LINK_TYPE
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_PHOTO_TYPE
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_TEXT_TYPE
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
open class PostConstructorFetcher(
    private val postService: PostService,
    private val messageSenderService: MessageSenderService,
    private val callbackDataService: CallbackDataService,
    private val updatesUtil: UpdatesUtil,
    basicErrorController: BasicErrorController,
): DefaultFetcher() {

    private companion object {
        const val POST_CREATE_CANCEL = "post_create_cancel"
        const val POST_ACTION_CANCEL = "post_action_cancel"
        const val POST_CHANGE_TEXT = "post_change_text"
        const val POST_CHANGE_PHOTO = "post_change_photo"
        const val POST_DELETE_PHOTO = "post_delete_photo"
        const val POST_PREVIEW = "post_preview"
        const val POST_ADD_BUTTON = "post_add_button"
        const val POST_BUTTON_SETTINGS_CONSOLE = "post_change_button_console"
        const val POST_CHANGE_BUTTON_CAPTION = "post_change_button_caption"
        const val POST_CHANGE_BUTTON_LINK = "post_change_button_link"

        const val MAX_BUTTONS_COUNT = 10
    }

    @InjectData
    private fun doFetch() {}

    @Callback(POST_CREATE_CANCEL)
    fun pcCancel(update: Update, post: PostDTO) {
        val newPost = deletePcConsole(update, post)
        postService.deletePost(newPost)
        // TODO: LUAT?
    }

    @Callback(POST_CHANGE_TEXT)
    fun pcChangeText(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)
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

        user.lastUserActionType = PC_TEXT_TYPE
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_CHANGE_PHOTO)
    fun pcChangePhoto(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        val newPost = deletePcConsole(update, post)
        val msgText = "Отправьте фотографию, которую вы хотите прикрепить к рассылке"
        val cancelButton = CallbackDataDTO(
            callbackData = POST_ACTION_CANCEL,
            metaText = "Отмена",
        ).save()
        val buttonsList = mutableListOf(listOf(cancelButton.createKeyboard()))
        if (newPost.imageHash != null) {
            val deletePhoto = CallbackDataDTO(
                callbackData = POST_DELETE_PHOTO,
                metaText = "Удалить фото",
            ).save()
            buttonsList.add(listOf(deletePhoto.createKeyboard()))
        }
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = msgText,
                    replyMarkup = createKeyboard(buttonsList),
                ),
            )
        user.lastUserActionType = PC_PHOTO_TYPE
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_DELETE_PHOTO)
    fun pcDeletePhoto(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        val newPost = post.copy(
            imageHash = null,
        )
        // TODO: show console
        user.lastUserActionType = DEFAULT_CREATE_POST
        return newPost
    }

    @Callback(POST_ACTION_CANCEL)
    fun pcCancelAction(update: Update, post: PostDTO, user: UserDTO) {
        user.lastUserActionType = DEFAULT_CREATE_POST
        // TODO: show console
    }

    @Callback(POST_PREVIEW)
    // TODO: buttons from context!
    fun pcPreview(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        // TODO: post sender service! Send  post
        val messageText = "<b>Конструктор постов</b>\n\nВыберите дальнейшее действие"
        val backToPc = CallbackDataDTO(
            callbackData = POST_ACTION_CANCEL,
            metaText = "Назад к конструктору",
        ).save()

        val keyboard = TODO("кнопки из контекста + backToPc")
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = messageText,
                    parseMode = ParseMode.HTML,
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_ADD_BUTTON)
    @Transactional
    open fun pcAddButton(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        val chatId = updatesUtil.getChatId(update)
        if (post.buttons.size >= MAX_BUTTONS_COUNT) {
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId!!,
                    text = "☠\uFE0F Ты добавил слишком много кнопок. Отредактируй или удали лишние",
                ),
            )
            return post
        }

        return changeButtonCaptionMessage(
            update = update,
            post = post,
            user = user,
            backToDefaultConsole = true,
        )
    }

    @Callback(POST_CHANGE_BUTTON_CAPTION)
    fun changeButtonCaptionMessage(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        return changeButtonCaptionMessage(
            update = update,
            post = post,
            user = user,
            backToDefaultConsole = false,
        )
    }

    @Callback(POST_CHANGE_BUTTON_LINK)
    fun changeButtonLink(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        deletePcConsole(update, post)
        val chatId = updatesUtil.getChatId(update)
        val backToBc =
            CallbackDataDTO(
                callbackData = POST_BUTTON_SETTINGS_CONSOLE,
                metaText = "К настройкам кнопки",
            ).save()
        val keyboard = listOf(listOf(backToBc.createKeyboard()))
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId!!,
                    text = "\uD83D\uDCDD Отправь мне текст с нужной ссылкой",
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        user.lastUserActionType = PC_BUTTON_LINK_TYPE
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    private fun changeButtonCaptionMessage(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        backToDefaultConsole: Boolean = false,
    ): PostDTO {
        val backToConsole =
            CallbackDataDTO(
                callbackData =
                if (backToDefaultConsole) {
                    POST_ACTION_CANCEL
                } else {
                    POST_BUTTON_SETTINGS_CONSOLE
                },
                metaText = if (backToDefaultConsole) "Отменить создание кнопки" else "К настройкам кнопки",
            ).save()
        val keyboard = listOf(listOf(backToConsole.createKeyboard()))
        deletePcConsole(update, post)
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = "\uD83D\uDCDD Отправь мне текст, который будет отображаться на кнопке",
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        user.lastUserActionType = PC_BUTTON_CAPTION_TYPE
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    fun deletePcConsole(update: Update, post: PostDTO): PostDTO =
        deletePcConsole(
            chatId = updatesUtil.getChatId(update),
            post,
        )

    fun deletePcConsole(chatId: String?, post: PostDTO): PostDTO {
        chatId ?: return post
        post.lastConsoleMessageId ?: return post
        messageSenderService.deleteMessage(
            MessageParams(
                chatId = chatId,
                messageId = post.lastConsoleMessageId,
            )
        )
        return post.copy(
            lastConsoleMessageId = null,
        ).save()
    }

    // TODO: show console

    private fun CallbackDataDTO.save() = callbackDataService.save(this)!!
    private fun PostDTO.save() = postService.save(this)

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) = InlineKeyboardMarkup().also { it.keyboard = keyboard }

}
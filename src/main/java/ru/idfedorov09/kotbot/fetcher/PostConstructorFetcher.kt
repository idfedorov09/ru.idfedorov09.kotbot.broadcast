package ru.idfedorov09.kotbot.fetcher

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.DEFAULT_CREATE_POST
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_PHOTO_TYPE
import ru.idfedorov09.kotbot.domain.dto.PostButtonDTO
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostButtonService
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.annotation.InputText
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.CallbackDataService
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import ru.idfedorov09.telegram.bot.base.util.UpdatesUtil
import ru.mephi.sno.libs.flow.belly.InjectData
import java.time.LocalDateTime
import java.time.ZoneId

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
    private val postButtonService: PostButtonService,
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
        const val POST_CHANGE_BUTTON_CALLBACK = "post_change_button_callback"
        const val POST_DELETE_BUTTON = "post_delete_button"
        const val POST_CHANGE_BUTTON = "post_change_button"
        const val POST_TOGGLE_PREVIEW = "post_toggle_preview"

        const val PC_TEXT_TYPE = "PC_TEXT_TYPE"
        const val PC_BUTTON_CAPTION_TYPE = "PC_BUTTON_CAPTION_TYPE"
        const val PC_BUTTON_LINK_TYPE = "PC_BUTTON_LINK_TYPE"
        const val PC_BUTTON_CALLBACK_TYPE = "PC_BUTTON_CALLBACK_TYPE"

        const val MAX_BUTTONS_COUNT = 10
        const val MAX_TEXT_SIZE_WITH_PHOTO = 900
        const val MAX_BTN_TEXT_SIZE = 32
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

        user.lastUserActionType = BroadcastLastUserActionType.PC_TEXT_TYPE
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
        user.lastUserActionType = BroadcastLastUserActionType.PC_BUTTON_LINK_TYPE
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_BUTTON_SETTINGS_CONSOLE)
    fun showChangeButtonConsole(
        update: Update,
        post: PostDTO,
    ): PostDTO {
        deletePcConsole(update, post)

        val userId = updatesUtil.getUserId(update)
        val chatId = updatesUtil.getChatId(update)
        val button = postButtonService
            .getLastModifiedButtonByUserId(userId!!.toLong())
            ?.copy(lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")))
            ?: return post

        val urlTextCode = button.link?.let { "<code>$it</code>" } ?: "пусто"
        val urlTextLink = button.link?.let { "(<a href='$it'>попробовать перейти</a>)" } ?: ""
        val caption = button.text?.let { "<code>$it</code>" } ?: "<b>текст не установлен!</b>"
        val callbackDataText = button.callbackData?.let { "<code>$it</code>" } ?: "<b>коллбэк не установлен</b>"

        val changeButtonCaption =
            CallbackDataDTO(
                callbackData = POST_CHANGE_BUTTON_CAPTION,
                metaText = button.text?.let { "Изменить текст" } ?: "Добавить текст",
            ).save()
        val changeButtonLink =
            CallbackDataDTO(
                callbackData = POST_CHANGE_BUTTON_LINK,
                metaText = button.link?.let { "Изменить ссылку" } ?: "Добавить ссылку",
            ).save()
        val changeButtonCallback =
            CallbackDataDTO(
                callbackData = POST_CHANGE_BUTTON_CALLBACK,
                metaText = button.callbackData?.let { "Изменить коллбэк" } ?: "Добавить коллбэк",
            ).save()
        val removeButton =
            CallbackDataDTO(
                callbackData = POST_DELETE_BUTTON,
                metaText = "Удалить кнопку",
            ).save()
        val backToBc =
            CallbackDataDTO(
                callbackData = POST_ACTION_CANCEL,
                metaText = "Назад к конструктору",
            ).save()
        val keyboard =
            listOf(changeButtonCaption, changeButtonLink, changeButtonCallback, removeButton, backToBc)
                .map { listOf(it.createKeyboard()) }
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId!!,
                    text =
                    "Настройки кнопки:\n\n" +
                            "Надпись на кнопке: $caption\n" +
                            "Ссылка: $urlTextCode $urlTextLink\n" +
                            "Коллбэк: $callbackDataText",
                    parseMode = ParseMode.HTML,
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        // TODO: last user action type
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    // TODO: callback params by name?
    @Callback(POST_CHANGE_BUTTON)
    fun editButton(
        update: Update,
        post: PostDTO,
        params: Map<String, String>,
    ) {
        val buttonId = params["buttonId"]?.toLongOrNull()!!
        postButtonService.updateButtonModifyTimeById(buttonId)
        showChangeButtonConsole(update, post)
    }

    @Callback(POST_DELETE_BUTTON)
    fun deleteButton(
        update: Update,
        post: PostDTO,
    ) {
        val userId = updatesUtil.getUserId(update)!!
        postButtonService.deleteLastModifiedButtonByUserId(userId.toLong())
        // TODO: show console
    }

    @Callback(POST_CHANGE_BUTTON_CALLBACK)
    fun changeButtonCallback(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        deletePcConsole(update, post)
        val chatId = updatesUtil.getChatId(update)!!
        val backToBc =
            CallbackDataDTO(
                callbackData = POST_BUTTON_SETTINGS_CONSOLE,
                metaText = "К настройкам кнопки",
            ).save()
        val keyboard = listOf(listOf(backToBc.createKeyboard()))
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId,
                    replyMarkup = createKeyboard(keyboard),
                    text = "\uD83D\uDCDD Отправь мне текст коллбэка",
                ),
            )
        user.lastUserActionType = BroadcastLastUserActionType.PC_BUTTON_CALLBACK_TYPE
        return post.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_TOGGLE_PREVIEW)
    fun pcToggleWebPreview(
        post: PostDTO,
    ): PostDTO {
        return post.copy(
            shouldShowWebPreview = !post.shouldShowWebPreview,
        ).save().also { TODO("show console") }
    }

    @InputText(PC_TEXT_TYPE)
    fun changeText(
        update: Update,
        post: PostDTO,
    ): PostDTO {
        var newPost = post
        val text = update.message.text
        val chatId = updatesUtil.getChatId(update)!!
        if (post.imageHash != null && text.length > MAX_TEXT_SIZE_WITH_PHOTO) {
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId,
                    text =
                    "Ошибка! Невозможно добавить текст длины" +
                            " ${text.length} > $MAX_TEXT_SIZE_WITH_PHOTO если приложена фотография. " +
                            "Измени текст или удали фотографию.",
                ),
            )
        } else {
            newPost = post.copy(
                text = text,
            )
        }
        // TODO: change LUAT to default
        // TODO: show console
        return newPost
    }

    @InputText(PC_BUTTON_CAPTION_TYPE)
    fun changeButtonCaption(
        update: Update,
        post: PostDTO,
    ) {
        deletePcConsole(update, post)
        val caption = update.message.text
        val chatId = updatesUtil.getChatId(update)!!
        val userId = updatesUtil.getUserId(update)!!
        if (caption.length >= MAX_BTN_TEXT_SIZE) {
            val backToBc =
                CallbackDataDTO(
                    callbackData = POST_BUTTON_SETTINGS_CONSOLE,
                    metaText = "К настройкам кнопки",
                ).save()
            val keyboard = listOf(listOf(backToBc.createKeyboard()))
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId,
                    replyMarkup = createKeyboard(keyboard),
                    text =
                    "\uD83E\uDD21 Слишком длинная надпись для кнопки! " +
                            "Ограничение на длину символов: $MAX_BTN_TEXT_SIZE. Повтори попытку.\n\n" +
                            "\uD83D\uDCDD Отправь мне текст, который будет отображаться на кнопке",
                ),
            )
            return
        }
        postButtonService
            .getLastModifiedButtonByUserId(userId.toLong())
            ?.copy(
                text = caption,
                lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
            )
            ?.save()
        showChangeButtonConsole(update, post)
    }

    @InputText(PC_BUTTON_LINK_TYPE)
    fun changeButtonLink(
        update: Update,
        post: PostDTO,
    ) {
        deletePcConsole(update, post)
        val newUrl = update.message.text
        val userId = updatesUtil.getUserId(update)!!
        postButtonService
            .getLastModifiedButtonByUserId(userId.toLong())
            ?.copy(
                link = newUrl,
                lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
            )
            ?.save()
        showChangeButtonConsole(update, post)
    }

    @InputText(PC_BUTTON_CALLBACK_TYPE)
    fun changeButtonCallback(
        update: Update,
        post: PostDTO,
    ) {
        deletePcConsole(update, post)
        val newCallbackData = update.message.text
        val userId = updatesUtil.getUserId(update)!!
        postButtonService
            .getLastModifiedButtonByUserId(userId.toLong())
            ?.copy(
                callbackData = newCallbackData,
                lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
            )
            ?.save()
        showChangeButtonConsole(update, post)
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
        user.lastUserActionType = BroadcastLastUserActionType.PC_BUTTON_CAPTION_TYPE
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
    private fun PostButtonDTO.save() = postButtonService.save(this)

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) = InlineKeyboardMarkup().also { it.keyboard = keyboard }

}
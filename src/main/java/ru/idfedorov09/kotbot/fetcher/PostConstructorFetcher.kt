package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.DEFAULT_CREATE_POST
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType.PC_NAME_TYPE
import ru.idfedorov09.kotbot.domain.GlobalConstants.getButtonIdParam
import ru.idfedorov09.kotbot.domain.GlobalConstants.getClassifier
import ru.idfedorov09.kotbot.domain.GlobalConstants.setButtonIdParam
import ru.idfedorov09.kotbot.domain.GlobalConstants.setClassifier
import ru.idfedorov09.kotbot.domain.PostClassifiers.defaultSaveClassifier
import ru.idfedorov09.kotbot.domain.PostClassifiers.savePostAndExitClassifier
import ru.idfedorov09.kotbot.domain.dto.PostButtonDTO
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostButtonService
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_CREATE_NEW_POST
import ru.idfedorov09.telegram.bot.base.config.registry.RegistryHolder
import ru.idfedorov09.telegram.bot.base.domain.LastUserActionTypes
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.annotation.InputPhoto
import ru.idfedorov09.telegram.bot.base.domain.annotation.InputText
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
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
class PostConstructorFetcher(
    private val postService: PostService,
    private val messageSenderService: MessageSenderService,
    private val updatesUtil: UpdatesUtil,
    private val postButtonService: PostButtonService,
): DefaultFetcher() {

    companion object {
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
        const val POST_CHANGE_NAME = "post_change_name"

        const val PC_TEXT_TYPE = "PC_TEXT_TYPE"
        const val PC_BUTTON_CAPTION_TYPE = "PC_BUTTON_CAPTION_TYPE"
        const val PC_BUTTON_LINK_TYPE = "PC_BUTTON_LINK_TYPE"
        const val PC_BUTTON_CALLBACK_TYPE = "PC_BUTTON_CALLBACK_TYPE"
        const val PC_PHOTO_TYPE = "PC_PHOTO_TYPE"

        const val MAX_BUTTONS_COUNT = 10
        const val MAX_TEXT_SIZE_WITHOUT_PHOTO = 900
        const val MAX_BTN_TEXT_SIZE = 32
    }

    @InjectData
    fun doFetch() {}

    @Callback(POST_CREATE_CANCEL)
    fun pcCancel(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ) {
        val newPost = deletePcConsole(update, post)
        postService.deletePost(newPost)
        user.lastUserActionType = LastUserActionTypes.DEFAULT
    }

    @Callback(POST_CHANGE_NAME)
    fun setPostName(
        update: Update,
        user: UserDTO,
        post: PostDTO,
        callbackData: CallbackDataDTO,
    ): PostDTO {
        val messageText = "<b>Конструктор постов</b>\n\nНапишите название поста"
        val newPost = deletePcConsole(update, post)
        messageSenderService.sendMessage(
            MessageParams(
                chatId = updatesUtil.getChatId(update)!!,
                text = messageText,
                parseMode = ParseMode.HTML,
            )
        )
        val classifier = RegistryHolder
            .getRegistry<PostClassifier>()
            .get(callbackData.getClassifier())
            ?: defaultSaveClassifier
        user.lastUserActionType = PC_NAME_TYPE.setClassifier(classifier)
        return newPost
    }

    @Callback(POST_CHANGE_TEXT)
    fun pcChangeText(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)
        // TODO: скрытый текст??
        val msgText =
            "*Напишите текст уведомления*\\.\n\nПравила оформления:\n" +
                    "<b\\>текст</b\\> \\- жирный текст\n" +
                    "<i\\>текст</i\\> \\- выделение курсивом\n" +
                    "<u\\>текст</u\\> \\- подчеркнутый текст\n" +
                    "<s\\>текст</s\\> \\- зачеркнутый текст\n" +
                    "<code\\>текст</code\\> \\- выделенный текст \\(с копированием по клику\\)\n" +
                    "<pre language\\=\"c\\+\\+\"\\>текст</pre\\> \\- исходный код или любой другой текст\n" +
                    "<a href\\='https://google\\.com/'\\>Гугл</a\\> \\- ссылка"
        // TODO: идея: картинки ссылками
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
        user.lastUserActionType = BroadcastLastUserActionType.PC_PHOTO_TYPE
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_DELETE_PHOTO)
    fun pcDeletePhoto(update: Update, post: PostDTO, user: UserDTO): PostDTO {
        val newPost = post.copy(
            imageHash = null,
        )
        showPcConsole(update, user, newPost)
        user.lastUserActionType = DEFAULT_CREATE_POST
        return newPost
    }

    @Callback(POST_ACTION_CANCEL)
    fun pcCancelAction(update: Update, post: PostDTO, user: UserDTO) {
        user.lastUserActionType = DEFAULT_CREATE_POST
        showPcConsole(update, user, post)
    }

    @Callback(POST_PREVIEW)
    fun pcPreview(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)
        val messageText = "<b>Конструктор постов</b>\n\nВыберите дальнейшее действие"
        postService.sendPost(user, newPost)

        val keyboard = RegistryHolder
            .getRegistry<PostClassifier>()
            .get(newPost.classifier)
            ?.createKeyboardAction
            ?.invoke(update, newPost, user, callbackData)
            ?.map { innerList ->
                innerList.map { callbackDTO ->
                    callbackDTO
                        .save()
                        .createKeyboard()
                }
            }
            ?: listOf()

        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = messageText,
                    parseMode = ParseMode.HTML,
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_ADD_BUTTON)
    fun pcAddButton(update: Update, post: PostDTO, user: UserDTO): PostDTO {
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

        post.buttons.add(
            PostButtonDTO(
                author = user,
            )
        )

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
    fun changeButtonLinkMessage(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)
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
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_BUTTON_SETTINGS_CONSOLE)
    fun showChangeButtonConsole(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)

        val chatId = updatesUtil.getChatId(update)
        val button = newPost.getLastModifiedButton()

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
            ).takeIf {
                button.link != null || button.callbackData != null
            }?.save()
        val keyboard =
            listOfNotNull(changeButtonCaption, changeButtonLink, changeButtonCallback, removeButton, backToBc)
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
        user.lastUserActionType = BroadcastLastUserActionType.DEFAULT_CREATE_POST
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_CHANGE_BUTTON)
    fun editButton(
        update: Update,
        post: PostDTO,
        user: UserDTO,
        callbackData: CallbackDataDTO,
    ) {
        val buttonId = callbackData
            .getButtonIdParam()
            ?.toLongOrNull()
            ?: return

        postButtonService.updateButtonModifyTimeById(buttonId)
        showChangeButtonConsole(update, post, user)
    }

    @Callback(POST_DELETE_BUTTON)
    fun deleteButton(
        update: Update,
        user: UserDTO,
        post: PostDTO,
    ) {
        postButtonService.deleteLastModifiedButtonByUserId(user.id!!)
        showPcConsole(update, user, post)
    }

    @Callback(POST_CHANGE_BUTTON_CALLBACK)
    fun changeButtonCallbackMessage(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        val newPost = deletePcConsole(update, post)
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
        return newPost.copy(
            lastConsoleMessageId = sent.messageId
        ).save()
    }

    @Callback(POST_TOGGLE_PREVIEW)
    fun pcToggleWebPreview(
        update: Update,
        user: UserDTO,
        post: PostDTO,
    ): PostDTO {
        return post.copy(
            shouldShowWebPreview = !post.shouldShowWebPreview,
        ).save().also {
            showPcConsole(update, user, it)
        }
    }

    @InputText(PC_TEXT_TYPE)
    fun changeText(
        update: Update,
        user: UserDTO,
        post: PostDTO,
    ): PostDTO {
        var newPost = post
        val text = update.message.text
        val chatId = updatesUtil.getChatId(update)!!
        if (post.imageHash != null && text.length > MAX_TEXT_SIZE_WITHOUT_PHOTO) {
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId,
                    text =
                    "Ошибка! Невозможно добавить текст длины" +
                            " ${text.length} > $MAX_TEXT_SIZE_WITHOUT_PHOTO если приложена фотография. " +
                            "Измени текст или удали фотографию.",
                ),
            )
        } else {
            newPost = post.copy(
                text = text,
            )
        }
        deleteUpdateMessage()
        showPcConsole(update, user, newPost)
        return newPost
    }

    @InputText(PC_BUTTON_CAPTION_TYPE)
    fun changeButtonCaption(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ) {
        val newPost = deletePcConsole(update, post)
        val caption = update.message.text
        val chatId = updatesUtil.getChatId(update)!!
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
        newPost.getLastModifiedButton().apply {
            text = caption
            lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
        }
        deleteUpdateMessage()
        showChangeButtonConsole(update, newPost, user)
    }

    @InputText(PC_BUTTON_LINK_TYPE)
    fun changeButtonLink(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ) {
        val newPost = deletePcConsole(update, post)
        val newUrl = update.message.text
        newPost.getLastModifiedButton().apply {
            link = newUrl
            lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
        }
        deleteUpdateMessage()
        showChangeButtonConsole(update, newPost, user)
    }

    @InputText(PC_BUTTON_CALLBACK_TYPE)
    fun changeButtonCallback(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ) {
        val newPost = deletePcConsole(update, post)
        val newCallbackData = update.message.text
        post.buttons
            .sortedBy { it.lastModifyTime }
            .first()
        newPost.getLastModifiedButton().apply {
            callbackData = newCallbackData
            lastModifyTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
        }
        deleteUpdateMessage()
        showChangeButtonConsole(update, newPost, user)
    }

    @InputPhoto(PC_PHOTO_TYPE)
    fun changePhoto(
        update: Update,
        post: PostDTO,
        user: UserDTO,
    ): PostDTO {
        var newPost = post
        val chatId = updatesUtil.getChatId(update)!!
        val postTextLength = newPost.text?.length ?: 0
        if (postTextLength > MAX_TEXT_SIZE_WITHOUT_PHOTO) {
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = chatId,
                    text =
                    "Ошибка! Невозможно добавить фотографию, длина текста " +
                            "$postTextLength > $MAX_TEXT_SIZE_WITHOUT_PHOTO. " +
                            "Измените текст или не прикладывайте фотографию",
                ),
            )
        } else {
            val photoId = update.message.photo.last().fileId
            newPost = post.copy(
                imageHash = photoId
            )
        }
        deleteUpdateMessage()
        showPcConsole(update, user, newPost)
        return newPost
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
        val newPost = deletePcConsole(update, post)
        val sent =
            messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = "\uD83D\uDCDD Отправь мне текст, который будет отображаться на кнопке",
                    replyMarkup = createKeyboard(keyboard),
                ),
            )
        user.lastUserActionType = BroadcastLastUserActionType.PC_BUTTON_CAPTION_TYPE
        return newPost.copy(
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

    @Callback(BROADCAST_CREATE_NEW_POST)
    fun showPcConsole(
        update: Update,
        user: UserDTO,
        post: PostDTO?,
        callbackData: CallbackDataDTO? = null,
    ): PostDTO {
        val hasCallback = update.hasCallbackQuery()
        val callbackMessageId =
            if (hasCallback)
                update.callbackQuery.message.messageId
            else
                null
        val chatId = updatesUtil.getChatId(update)!!
        var currentPost: PostDTO? = post
        if (currentPost == null) {
            // TODO: алерт если есть посты с isCurrent (такой ситуации теоретически не должно быть) ?
            val classifier = callbackData?.getClassifier()
            currentPost = postService.save(
                PostDTO(
                    author = user,
                    lastConsoleMessageId = callbackMessageId,
                    shouldShowWebPreview = false,
                    classifier = classifier,
                )
            )
            val messageText = "<b>Конструктор постов</b>\n\nВыберите дальнейшее действие"
            val newPhoto = CallbackDataDTO(callbackData = POST_CHANGE_PHOTO, metaText = "Добавить фото").save()
            val addText = CallbackDataDTO(callbackData = POST_CHANGE_TEXT, metaText = "Добавить текст").save()
            val addButton = CallbackDataDTO(callbackData = POST_ADD_BUTTON, metaText = "Добавить кнопку").save()
            val webPreviewButton = createWebPreviewToggleButton(currentPost)
            val cancelButton = CallbackDataDTO(callbackData = POST_CREATE_CANCEL, metaText = "Отмена").save()

            val keyboard =
                listOfNotNull(newPhoto, addText, addButton, webPreviewButton, cancelButton)
                    .map { listOf(it.createKeyboard()) }
            if (hasCallback) {
                messageSenderService.editMessage(
                    MessageParams(
                        messageId = callbackMessageId,
                        text = messageText,
                        parseMode = ParseMode.HTML,
                        replyMarkup = createKeyboard(keyboard),
                        chatId = chatId,
                        disableWebPagePreview = !currentPost.shouldShowWebPreview,
                    ),
                )
            } else {
                val sent =
                    messageSenderService.sendMessage(
                        MessageParams(
                            text = messageText,
                            parseMode = ParseMode.HTML,
                            replyMarkup = createKeyboard(keyboard),
                            chatId = chatId,
                            disableWebPagePreview = !currentPost.shouldShowWebPreview,
                        ),
                    )
                currentPost = currentPost.copy(
                    lastConsoleMessageId = sent.messageId
                )
            }
        } else {
            currentPost = deletePcConsole(update, currentPost)
            val photoProp =
                CallbackDataDTO(
                    callbackData = POST_CHANGE_PHOTO,
                    metaText = currentPost.imageHash?.let { "Изменить фото" } ?: "Добавить фото",
                ).save()
            val textProp =
                CallbackDataDTO(
                    callbackData = POST_CHANGE_TEXT,
                    metaText = currentPost.text?.let { "Изменить текст" } ?: "Добавить текст",
                ).save()
            val addButton = CallbackDataDTO(callbackData = POST_ADD_BUTTON, metaText = "Добавить кнопку").save()
            val webPreviewButton = createWebPreviewToggleButton(currentPost)
            val previewButton = CallbackDataDTO(callbackData = POST_PREVIEW, metaText = "Предпросмотр").save()
            val cancelButton = CallbackDataDTO(callbackData = POST_CREATE_CANCEL, metaText = "Отмена").save()

            val keyboardList =
                listOfNotNull(
                    photoProp,
                    textProp,
                    addButton,
                    webPreviewButton,
                    previewButton,
                ).toMutableList().apply {
                    addAll(
                        postButtonService.findAllValidButtonsForPost(currentPost!!.id!!).map {
                            CallbackDataDTO(
                                callbackData = POST_CHANGE_BUTTON,
                                metaText = it.text,
                            )
                                .setButtonIdParam(it.id!!)
                                .save()
                        },
                    )
                }
            keyboardList.add(cancelButton)
            val keyboard =
                keyboardList.apply {
                    if (currentPost!!.imageHash == null && currentPost!!.text == null) {
                        remove(previewButton)
                    }
                    if (currentPost!!.buttons.size >= MAX_BUTTONS_COUNT) {
                        remove(addButton)
                    }
                }.map {
                    listOf(it.createKeyboard())
                }
            val text =
                currentPost.run {
                    val title = "<b>Конструктор постов</b>\n\n"
                    val text = text?.let { "Текст:\n${text}\n\n" } ?: ""
                    val end = "Выберите дальнейшее действие"
                    title + text + end
                }

            runCatching {
                when (currentPost!!.imageHash) {
                    null ->
                        messageSenderService.sendMessage(
                            MessageParams(
                                chatId = chatId,
                                text = text,
                                replyMarkup = createKeyboard(keyboard),
                                parseMode = ParseMode.HTML,
                                disableWebPagePreview = !currentPost!!.shouldShowWebPreview,
                            ),
                        )

                    else ->
                        messageSenderService.sendMessage(
                            MessageParams(
                                chatId = chatId,
                                text = text,
                                parseMode = ParseMode.HTML,
                                replyMarkup = createKeyboard(keyboard),
                                photo = InputFile(currentPost!!.imageHash),
                                disableWebPagePreview = !currentPost!!.shouldShowWebPreview,
                            ),
                        )
                }
            }.onFailure {
                val failText =
                    "\uD83D\uDE4A Ой! При отправке сообщения что-то пошло не так:\n" +
                            "<pre language=\"error\">${
                                it.message
                                    ?.replace("<", "&lt;")
                                    ?.replace(">", "&gt;")
                            }</pre>\n\nПопробуй еще раз."

                messageSenderService.sendMessage(
                    MessageParams(
                        text = failText,
                        chatId = chatId,
                        parseMode = ParseMode.HTML,
                    ),
                )
            }.onSuccess {
                currentPost = currentPost!!.copy(
                    lastConsoleMessageId = it.messageId,
                )
            }
        }
        user.lastUserActionType = LastUserActionTypes.DEFAULT
        return currentPost!!.save()
    }

    private fun createWebPreviewToggleButton(post: PostDTO): CallbackDataDTO {
//        if (post.isWeekly) return null
        val smile = if (post.shouldShowWebPreview) "✅" else "❌"
        val state = if (post.shouldShowWebPreview) "(вкл)" else "(выкл)"
        val text = "$smile Превью веб-страницы $state"
        return CallbackDataDTO(callbackData = POST_TOGGLE_PREVIEW, metaText = text).save()
    }
}
package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.kotbot.domain.GlobalConstants.getCurrentPage
import ru.idfedorov09.kotbot.domain.GlobalConstants.setClassifier
import ru.idfedorov09.kotbot.domain.GlobalConstants.setCurrentPage
import ru.idfedorov09.kotbot.domain.GlobalConstants.setPostId
import ru.idfedorov09.kotbot.domain.PostClassifiers.lastBroadcastStepClassifier
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.PostService
import ru.idfedorov09.telegram.bot.base.domain.LastUserActionTypes
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.annotation.Command
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.executor.Executor
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import ru.idfedorov09.telegram.bot.base.util.UpdatesUtil
import ru.mephi.sno.libs.flow.belly.InjectData

@Component
open class BroadcastConstructorFetcher(
    private val messageSenderService: MessageSenderService,
    private val updatesUtil: UpdatesUtil,
    private val postService: PostService,
    private val bot: Executor,
) : DefaultFetcher() {

    companion object {
        const val BROADCAST_SELECT_POST = "bc_select_existing_post"
        const val BROADCAST_CREATE_NEW_POST = "bc_create_new_post"
        const val BROADCAST_TRY_TO_EDIT_POST = "bc_try_to_edit_post"
        const val BROADCAST_GET_TARGET_POST = "bc_get_target_post"
        const val BROADCAST_BACK_TO_HANDLER_MENU = "bc_back_to_handler"
        const val BROADCAST_CREATE_CANCEL = "bc_create_cancel" // TODO: create
        const val BROADCAST_SEND_NOW = "bc_send_post_now" // TODO: create
        const val BROADCAST_SCHEDULE_SEND = "bc_schedule_send" // TODO: create
    }

    @InjectData
    fun doFetch() {}

    @Command("/create_broadcast")
    @Callback(BROADCAST_BACK_TO_HANDLER_MENU)
    suspend fun broadcastEntry(
        update: Update,
        user: UserDTO,
    ) {
        val chatId = updatesUtil.getChatId(update)!!
        val selectExistingPostButton = CallbackDataDTO(
            callbackData = BROADCAST_SELECT_POST,
            metaText = "Выбрать пост"
        ).setClassifier(lastBroadcastStepClassifier).save()
        val createNewPostButton = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_NEW_POST,
            metaText = "Создать новый пост"
        ).setClassifier(lastBroadcastStepClassifier).save()
        val cancelButton = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_CANCEL,
            metaText = "Отмена"
        ).save()
        val keyboard =
            listOf(selectExistingPostButton, createNewPostButton, cancelButton)
                .map { listOf(it.createKeyboard()) }
        val messageText = "<b>Конструктор рассылки</b>\n\nВыберите дальнейшее действие"

        deleteUpdateMessage()
        messageSenderService.sendMessage(
            MessageParams(
                chatId = chatId,
                text = messageText,
                parseMode = ParseMode.HTML,
                replyMarkup = createKeyboard(keyboard),
            )
        )
        user.lastUserActionType = BroadcastLastUserActionType.ENTRY_CREATE_POST
    }

    @Callback(BROADCAST_SELECT_POST)
    fun selectExistingPost(
        update: Update,
        callbackDataDTO: CallbackDataDTO,
    ) {
        val chatId = updatesUtil.getChatId(update)!!
        val pagesCount = postService.lastPageNum()
        if (pagesCount < 0) {
            val callbackAnswer =
                AnswerCallbackQuery().also {
                    it.text = "\uD83D\uDEAB Постов не найдено!"
                    it.callbackQueryId = update.callbackQuery.id
                    it.showAlert = true
                }
            bot.execute(callbackAnswer)
            return
        }
        val currentPage = callbackDataDTO.getCurrentPage()?.toIntOrNull() ?: 0

        val messageText = "<b>Конструктор рассылки</b>\n\nВыберите пост"
        val backButton = CallbackDataDTO(
            callbackData = BROADCAST_SELECT_POST,
            metaText = "◀\uFE0F"
        ).setCurrentPage(currentPage - 1).takeIf { currentPage > 0 }?.save()
        val nextButton = CallbackDataDTO(
            callbackData = BROADCAST_SELECT_POST,
            metaText = "▶\uFE0F"
        ).setCurrentPage(currentPage + 1).takeIf { currentPage < pagesCount }?.save()
        val backToMenu = CallbackDataDTO(
            callbackData = BROADCAST_BACK_TO_HANDLER_MENU,
            metaText = "Назад",
        ).save()
        val posts = postService
            .findAvailablePostsOnPage(currentPage)
            .filter { it.name != null }
            .map {
                CallbackDataDTO(
                    callbackData = BROADCAST_GET_TARGET_POST,
                    metaText = it.name,
                )
                    .setClassifier(lastBroadcastStepClassifier)
                    .setPostId(it.id!!)
                    .save()
            }

        val keyboard = posts
            .map { listOf(it.createKeyboard()) }
            .plusElement(
                listOfNotNull(
                    backButton?.createKeyboard(),
                    nextButton?.createKeyboard(),
                )
            ).plusElement(
                listOf(
                    backToMenu.createKeyboard()
                )
            )

        messageSenderService.editMessage(
            MessageParams(
                chatId = chatId,
                messageId = update.callbackQuery.message.messageId,
                text = messageText,
                parseMode = ParseMode.HTML,
                replyMarkup = createKeyboard(keyboard),
            )
        )
        // TODO: luat?
    }

    // TODO
//    @Callback(POST_BROADCAST_SAVE_POST_AND_EXIT)
    suspend fun savePostAndExit(
        update: Update,
        user: UserDTO,
        broadcastDataDTO: BroadcastDataDTO,
        post: PostDTO,
    ) {
        if (update.hasCallbackQuery()) {
            val callbackAnswer =
                AnswerCallbackQuery().also {
                    it.text = "✅ Сохранено"
                    it.callbackQueryId = update.callbackQuery.id
                }
            bot.execute(callbackAnswer)
        }

        val chatId = updatesUtil.getChatId(update)!!
        post.lastConsoleMessageId?.let {
            messageSenderService.deleteMessage(
                MessageParams(
                    chatId = chatId,
                    messageId = it
                )
            )
        }
        post.copy(
            isBuilt = true,
            lastConsoleMessageId = null,
        ).save().also { addToContext(it) }
        broadcastDataDTO.copy(
            currentPost = null,
        ).save().also { addToContext(it) }
        user.lastUserActionType = LastUserActionTypes.DEFAULT
    }
}
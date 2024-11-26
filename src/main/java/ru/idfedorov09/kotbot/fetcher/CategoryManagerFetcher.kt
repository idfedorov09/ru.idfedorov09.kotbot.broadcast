package ru.idfedorov09.kotbot.fetcher

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.kotbot.domain.GlobalConstants.CATEGORY_TITLE
import ru.idfedorov09.kotbot.domain.GlobalConstants.TARGET_MESSAGE_ID
import ru.idfedorov09.kotbot.domain.GlobalConstants.getCurrentPage
import ru.idfedorov09.kotbot.domain.GlobalConstants.setCategoryId
import ru.idfedorov09.kotbot.domain.GlobalConstants.setCurrentPage
import ru.idfedorov09.kotbot.domain.GlobalConstants.setParam
import ru.idfedorov09.kotbot.domain.service.CategoryService
import ru.idfedorov09.kotbot.fetcher.BroadcastConstructorFetcher.Companion.BROADCAST_BACK_TO_HANDLER_MENU
import ru.idfedorov09.telegram.bot.base.config.registry.LastUserActionType
import ru.idfedorov09.telegram.bot.base.domain.LastUserActionTypes
import ru.idfedorov09.telegram.bot.base.domain.annotation.Callback
import ru.idfedorov09.telegram.bot.base.domain.annotation.Command
import ru.idfedorov09.telegram.bot.base.domain.annotation.InputText
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import ru.idfedorov09.telegram.bot.base.util.UpdatesUtil
import ru.mephi.sno.libs.flow.belly.InjectData
import kotlin.coroutines.coroutineContext

/**
 * Фетчер, отвечающий за создание/изменение категорий
 */
@Component
class CategoryManagerFetcher(
    private val updatesUtil: UpdatesUtil,
    private val messageSenderService: MessageSenderService,
    private val categoryService: CategoryService,
): DefaultFetcher() {

    companion object {
        const val CATEGORY_SELECT_EXIST = "category_select_exist"
        const val CATEGORY_GET_TARGET = "category_get_target"
        const val CATEGORY_CREATE_NEW = "category_create_new"
        const val CATEGORY_CREATE_CANCEL = "category_create_cancel"

        const val CAT_NAME_TYPE = "CAT_NAME_TYPE"
        const val CAT_DESCRIPTION_TYPE = "CAT_DESCRIPTION_TYPE"

        const val CATEGORY_NAME_MAX_LENGTH = 32
    }

    @InjectData
    fun doFetch() {}

    @Command("/manage_categories")
    @Callback(CATEGORY_SELECT_EXIST)
    suspend fun manageCategories(
        update: Update,
        user: UserDTO,
        callbackDataDTO: CallbackDataDTO? = null,
    ) {
        if (user.lastUserActionType != LastUserActionTypes.DEFAULT)
            return

        val messageText = "<b>Управление категориями</b>\n\nВыберите категорию или создайте новую:"
        val pagesCount = categoryService.lastPageNum()
        val currentPage = callbackDataDTO?.getCurrentPage()?.toIntOrNull() ?: 0

        val backButton = CallbackDataDTO(
            callbackData = CATEGORY_SELECT_EXIST,
            metaText = "◀\uFE0F"
        ).setCurrentPage(currentPage - 1).takeIf { currentPage > 0 }?.save()
        val nextButton = CallbackDataDTO(
            callbackData = CATEGORY_SELECT_EXIST,
            metaText = "▶\uFE0F"
        ).setCurrentPage(currentPage + 1).takeIf { currentPage < pagesCount }?.save()
        val backToMenu = CallbackDataDTO(
            callbackData = BROADCAST_BACK_TO_HANDLER_MENU, // TODO
            metaText = "Назад",
        ).save() // TODO: нужна ли вообще эта кнопка? предлагаю не менять luat в этой меню и она тогда вообще не нужна оказывается

        val createNewCategoryButton = CallbackDataDTO(
            callbackData = CATEGORY_CREATE_NEW,
            metaText = "➕ Новая категория",
        ).save()

        val categories = categoryService
            .findAvailableCategoriesOnPage(currentPage)
            .filter { it.name != null }
            .map {
                CallbackDataDTO(
                    callbackData = CATEGORY_GET_TARGET,
                    metaText = it.name,
                )
                    .setCategoryId(it.id!!)
                    .save()
            }

        val keyboard = categories
            .map { listOf(it.createKeyboard()) }
            .plusElement(
                listOfNotNull(
                    backButton?.createKeyboard(),
                    nextButton?.createKeyboard(),
                )
            )
            .plusElement(listOf(backToMenu.createKeyboard()))
            .plusElement(listOf(createNewCategoryButton.createKeyboard()))

        val messageParams = MessageParams(
            chatId = updatesUtil.getChatId(update)!!,
            messageId = update.callbackQuery?.message?.messageId,
            text = messageText,
            parseMode = ParseMode.HTML,
            replyMarkup = createKeyboard(keyboard),
        )
        if (update.hasCallbackQuery()) {
            messageSenderService.editMessage(messageParams)
        } else {
            deleteUpdateMessage()
            messageSenderService.sendMessage(messageParams)
        }
    }

    @Callback(CATEGORY_CREATE_NEW)
    suspend fun createNewCategoryEntry(
        update: Update,
        user: UserDTO,
    ) {
        if (user.lastUserActionType != LastUserActionTypes.DEFAULT)
            return
        enterCategoryName(user, update, update.callbackQuery.message.messageId)
    }

    private suspend fun enterCategoryName(
        user: UserDTO,
        update: Update,
        consoleMessageId: Int? = null,
    ) {
        val actualConsoleMessageId = consoleMessageId ?: user.lastUserActionType?.getTargetMessageId()
        user.lastUserActionType = BroadcastLastUserActionType.CAT_NAME_TYPE.copy()
        actualConsoleMessageId?.let { user.lastUserActionType?.setTargetMessageId(it) }

        val messageParams = MessageParams(
            chatId = updatesUtil.getChatId(update)!!,
            messageId = actualConsoleMessageId,
            text = "<b>Конструктор категорий</b>\n\nВведите название категории",
            parseMode = ParseMode.HTML,
            replyMarkup = createKeyboard(createCancelKeyboard()),
        )

        if(actualConsoleMessageId == null) {
            val sent = messageSenderService.sendMessage(messageParams)
            user.lastUserActionType?.setTargetMessageId(sent.messageId)
        } else {
            messageSenderService.editMessage(messageParams)
        }
    }

    @InputText(CAT_NAME_TYPE)
    suspend fun enterCategoryNameHandler(
        user: UserDTO,
        update: Update,
    ) {
        deleteUpdateMessage()
        val categoryName = update.message?.text?.trim() ?: return

        if (categoryName.length > CATEGORY_NAME_MAX_LENGTH) {
            val sent = messageSenderService.sendMessage(
                MessageParams(
                    chatId = updatesUtil.getChatId(update)!!,
                    text = "\uD83D\uDEAB Слишком длинное название. " +
                            "Максимально допустимая длина - $CATEGORY_NAME_MAX_LENGTH.",
                )
            )
            coroutineScope {
                async {
                    delay(3000)
                    messageSenderService.deleteMessage(
                        MessageParams(
                            chatId = updatesUtil.getChatId(update)!!,
                            messageId = sent.messageId
                        )
                    )
                }
            }
            return
        }
        val sent = sendOrEditMessage(
            user,
            MessageParams(
                chatId = updatesUtil.getChatId(update)!!,
                text = "<b>Конструктор категорий [<code>$categoryName</code>]</b>\n\nВведите описание категории",
                replyMarkup = createKeyboard(createCancelKeyboard()),
                parseMode = ParseMode.HTML,
            )
        )

        user
            .setLastUserActionType(BroadcastLastUserActionType.CAT_DESCRIPTION_TYPE)
            .lastUserActionType!!
            .setTargetMessageId(sent.messageId)
            .setCategoryTitle(categoryName)
    }

    private suspend fun sendOrEditMessage(
        user: UserDTO,
        messageParams: MessageParams,
    ): Message {
        val consoleMessageId = user.lastUserActionType?.getTargetMessageId()
        if (consoleMessageId == null) {
            val sent = messageSenderService.sendMessage(
                messageParams.copy(messageId = null)
            )
            user.lastUserActionType?.setTargetMessageId(sent.messageId)
            return sent
        }
        return messageSenderService.editMessage(
            messageParams.copy(messageId = consoleMessageId)
        )
    }

    @Callback(CATEGORY_CREATE_CANCEL)
    suspend fun cancelCategoryHandler(
        user: UserDTO,
    ) {
        deleteUpdateMessage()
        user.lastUserActionType = LastUserActionTypes.DEFAULT
    }

    @InputText(CAT_DESCRIPTION_TYPE)
    suspend fun enterCategoryDescriptionHandler() {
        // TODO: дописать
    }

    private fun createCancelKeyboard(): List<List<InlineKeyboardButton>> {
        val cancelButton = CallbackDataDTO(
            callbackData = CATEGORY_CREATE_CANCEL,
            metaText = "Отмена"
        ).save()
        return listOf(listOf(cancelButton.createKeyboard()))
    }

    fun LastUserActionType.setTargetMessageId(messageId: Int) = setParam(TARGET_MESSAGE_ID, messageId)
    fun LastUserActionType.setCategoryTitle(title: String) = setParam(CATEGORY_TITLE, title)

    fun LastUserActionType.getTargetMessageId() = type.getParams().get(TARGET_MESSAGE_ID)?.toIntOrNull()
    fun LastUserActionType.getCategoryTitle() = type.getParams().get(CATEGORY_TITLE)
}
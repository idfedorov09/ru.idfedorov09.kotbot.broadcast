package ru.idfedorov09.kotbot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.telegram.bot.base.domain.annotation.Command
import ru.idfedorov09.telegram.bot.base.domain.dto.CallbackDataDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import ru.idfedorov09.telegram.bot.base.util.UpdatesUtil
import ru.mephi.sno.libs.flow.belly.InjectData

@Component
open class BroadcastConstructorFetcher(
    private val messageSenderService: MessageSenderService,
    private val updatesUtil: UpdatesUtil,
) : DefaultFetcher() {

    companion object {
        const val BROADCAST_SELECT_POST = "bc_select_existing_post" // TODO: create
        const val BROADCAST_CREATE_NEW_POST = "bc_create_new_post"
        const val BROADCAST_CREATE_CANCEL = "bc_create_cancel" // TODO: create
        val broadcastClassifierParam = "classifier" to "broadcast" // TODO: "classifier" to const
        private val log = LoggerFactory.getLogger(DefaultFetcher::class.java)
    }

    @InjectData
    fun doFetch() {
        log.info("test doti")
    }

    @Command("/create_broadcast")
    fun broadcastEntry(
        update: Update,
        user: UserDTO,
    ) {
        val chatId = updatesUtil.getChatId(update)!!

        val selectExistingPostButton = CallbackDataDTO(
            callbackData = BROADCAST_SELECT_POST,
            metaText = "Выбрать пост"
        ).addParameters(broadcastClassifierParam).save()
        val createNewPostButton = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_NEW_POST,
            metaText = "Создать новый пост"
        ).addParameters(broadcastClassifierParam).save()
        val cancelButton = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_CANCEL,
            metaText = "Отмена"
        ).save()
        val keyboard =
            listOf(selectExistingPostButton, createNewPostButton, cancelButton)
                .map { listOf(it.createKeyboard()) }
        val messageText = "<b>Конструктор рассылки</b>\n\nВыберите дальнейшее действие"

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
}
package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.kotbot.config.registry.PostClassifier
import ru.idfedorov09.kotbot.domain.BroadcastLastUserActionType
import ru.idfedorov09.kotbot.domain.PostClassifiers.choosePost
import ru.idfedorov09.kotbot.domain.PostClassifiers.createNewPost
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
        const val BROADCAST_SEND_NOW = "bc_send_post_now" // TODO: create
        const val BROADCAST_SCHEDULE_SEND = "bc_schedule_send" // TODO: create
        const val POST_BROADCAST_SAVE_POST_AND_EXIT = "bc_save_post_and_exit"
    }

    @InjectData
    fun doFetch() {}

    @Command("/create_broadcast")
    fun broadcastEntry(
        update: Update,
        user: UserDTO,
    ) {
        val chatId = updatesUtil.getChatId(update)!!

        val selectExistingPostButton = CallbackDataDTO(
            callbackData = BROADCAST_SELECT_POST,
            metaText = "Выбрать пост"
        ).setClassifier(choosePost).save()
        val createNewPostButton = CallbackDataDTO(
            callbackData = BROADCAST_CREATE_NEW_POST,
            metaText = "Создать новый пост"
        ).setClassifier(createNewPost).save()
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

    private fun CallbackDataDTO.setClassifier(classifier: PostClassifier) =
        addParameters(PostClassifier.mark to classifier.type)
}
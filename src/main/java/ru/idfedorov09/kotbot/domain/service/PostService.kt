package ru.idfedorov09.kotbot.domain.service

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.entity.PostEntity
import ru.idfedorov09.kotbot.repository.PostRepository
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO
import ru.idfedorov09.telegram.bot.base.domain.service.MessageSenderService
import ru.idfedorov09.telegram.bot.base.util.MessageParams
import kotlin.jvm.optionals.getOrNull

@Service
open class PostService(
    private val entityManager: EntityManager,
    private val messageSenderService: MessageSenderService,
    private val postRepository: PostRepository<PostEntity>,
) {

    open fun deletePost(post: PostDTO) {
        post.id ?: return
        postRepository.updateIsDeleted(
            postId = post.id,
            isDeleted = true,
        )
    }

    open fun findCurrentPostByAuthorId(authorId: Long): PostDTO? {
        return postRepository
            .findCurrentPostByAuthorId(authorId)
            ?.toDTO()
    }

    open fun findByPostId(postId: Long) = postRepository.findById(postId).getOrNull()?.toDTO()

    @Transactional
    open fun save(postEntity: PostEntity): PostEntity {
        postEntity.author = entityManager.merge(postEntity.author)
        postEntity.buttons.forEach { it.author = entityManager.merge(it.author) }
        return postRepository.save(postEntity)
    }

    @Transactional
    open fun save(post: PostDTO) = save(post.toEntity()).toDTO()

    open fun sendPost(
        user: UserDTO,
        post: PostDTO,
        parseMode: String = ParseMode.HTML,
    ) {
        messageSenderService.sendMessage(
            MessageParams(
                chatId = user.tui!!,
                text = post.text,
                replyMarkup = createKeyboard(post.buttons.map { listOf(it.createKeyboard()) }),
                parseMode = parseMode,
                photo = post.imageHash?.let { InputFile(it) },
                disableWebPagePreview = !post.shouldShowWebPreview
            )
        )
    }

    open fun findAvailablePostById(postId: Long) = postRepository.findAvailablePostById(postId)?.toDTO()
    open fun findAvailablePostsOnPage(pageNum: Int) = postRepository.findAvailablePostsOnPage(pageNum)
    open fun lastPageNum() = postRepository.lastPageNum()

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }
}
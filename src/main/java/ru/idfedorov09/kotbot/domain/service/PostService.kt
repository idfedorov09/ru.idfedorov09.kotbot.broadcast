package ru.idfedorov09.kotbot.domain.service

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.entity.PostEntity
import ru.idfedorov09.kotbot.repository.PostRepository

@Service
open class PostService(
    private val entityManager: EntityManager
) {

    @Autowired
    private lateinit var postRepository: PostRepository<PostEntity>

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

    @Transactional
    open fun save(postEntity: PostEntity): PostEntity {
        postEntity.author = entityManager.merge(postEntity.author)
        postEntity.buttons.forEach { it.author = entityManager.merge(it.author) }
        return postRepository.save(postEntity)
    }

    @Transactional
    open fun save(post: PostDTO) = save(post.toEntity()).toDTO()
}
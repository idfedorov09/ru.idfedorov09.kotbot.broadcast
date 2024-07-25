package ru.idfedorov09.kotbot.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.entity.PostEntity
import ru.idfedorov09.kotbot.repository.PostRepository

@Service
class PostService {

    @Autowired
    private lateinit var postRepository: PostRepository<PostEntity>

    fun deletePost(post: PostDTO) {
        post.id ?: return
        postRepository.updateIsDeleted(
            postId = post.id,
            isDeleted = true,
        )
    }

    fun save(postEntity: PostEntity) = postRepository.save(postEntity)

    fun save(post: PostDTO) = save(post.toEntity()).toDTO()
}
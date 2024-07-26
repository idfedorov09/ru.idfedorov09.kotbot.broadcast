package ru.idfedorov09.kotbot.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.idfedorov09.kotbot.domain.dto.PostButtonDTO
import ru.idfedorov09.kotbot.domain.entity.PostButtonEntity
import ru.idfedorov09.kotbot.repository.PostButtonRepository

@Service
class PostButtonService {

    @Autowired
    private lateinit var postButtonRepository: PostButtonRepository<PostButtonEntity>

    fun getLastModifiedButtonByUserId(userId: Long): PostButtonDTO? {
        return postButtonRepository.getLastModifiedButtonByUserId(userId)?.toDTO()
    }
}
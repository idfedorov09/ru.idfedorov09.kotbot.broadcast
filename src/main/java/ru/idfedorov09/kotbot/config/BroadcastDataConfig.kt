package ru.idfedorov09.kotbot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.kotbot.domain.service.BroadcastDataService
import ru.idfedorov09.kotbot.domain.service.PostService

@Configuration
open class BroadcastDataConfig {
    @Bean
    open fun postDTOInitializer(
        postService: PostService,
    ): PostDTO.Companion {
        PostDTO.init(postService)
        return PostDTO
    }

    @Bean
    open fun broadcastDataDTOInitializer(
        broadcastDataService: BroadcastDataService,
    ): BroadcastDataDTO.Companion {
        BroadcastDataDTO.init(broadcastDataService)
        return BroadcastDataDTO
    }
}
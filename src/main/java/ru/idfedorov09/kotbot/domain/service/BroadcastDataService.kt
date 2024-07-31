package ru.idfedorov09.kotbot.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.kotbot.domain.entity.BroadcastDataEntity
import ru.idfedorov09.kotbot.repository.BroadcastDataRepository
import kotlin.jvm.optionals.getOrNull

@Service
class BroadcastDataService {

    @Autowired
    private lateinit var broadcastDataRepository: BroadcastDataRepository<BroadcastDataEntity>

    fun save(broadcastDataEntity: BroadcastDataEntity) = broadcastDataRepository.save(broadcastDataEntity)
    fun save(broadcastDataDTO: BroadcastDataDTO) =
        save(broadcastDataDTO.toEntity()).toDTO()

    fun getBroadcastDataByUserId(userId: Long) =
        broadcastDataRepository.findById(userId).getOrNull()?.toDTO()
}
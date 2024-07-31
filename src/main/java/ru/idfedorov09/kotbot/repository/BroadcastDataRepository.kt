package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.idfedorov09.kotbot.domain.entity.BroadcastDataEntity

interface BroadcastDataRepository<T: BroadcastDataEntity> : JpaRepository<T, Long> {
}
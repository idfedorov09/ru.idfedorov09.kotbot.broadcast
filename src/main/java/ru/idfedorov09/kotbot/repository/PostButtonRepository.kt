package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.idfedorov09.kotbot.domain.entity.PostButtonEntity

interface PostButtonRepository<T: PostButtonEntity> : JpaRepository<T, Long> {

    @Query(
        """
            SELECT *
                FROM button
                WHERE author_id = :userId
                ORDER BY last_modify_dttm DESC 
            LIMIT 1
        """,
        nativeQuery = true,
    )
    fun getLastModifiedButtonByUserId(userId: Long): PostButtonEntity?

    @Query(
        """
            UPDATE button
            SET last_modify_dttm = CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'
            WHERE button_id = :buttonId
        """,
        nativeQuery = true,
    )
    fun updateButton(buttonId: Long)
}
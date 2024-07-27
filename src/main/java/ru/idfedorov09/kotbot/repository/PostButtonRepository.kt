package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.idfedorov09.kotbot.domain.entity.PostButtonEntity
import ru.idfedorov09.kotbot.fetcher.PostConstructorFetcher.Companion.MAX_BUTTONS_COUNT

interface PostButtonRepository<T: PostButtonEntity> : JpaRepository<T, Long> {

    @Query(
        """
            SELECT *
                FROM button
                WHERE 1=1
                    AND author_id = :userId
                    AND is_deleted = false
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

    @Query(
        """
            UPDATE button
            SET is_deleted = true
            WHERE button_id = (
                SELECT button_id
                FROM button
                WHERE author_id = :userId
                  AND is_deleted = false
                ORDER BY last_modify_dttm DESC
                LIMIT 1
            )
        """,
        nativeQuery = true,
    )
    fun deleteLastModifiedButtonByUserId(userId: Long)


    // join с таблицей post
    @Query(
        """
            SELECT *
                FROM button
                WHERE 1=1
                    AND is_deleted = false
                    AND post_id = :postId
                ORDER BY button_id
            LIMIT ${MAX_BUTTONS_COUNT + 1}
        """,
        nativeQuery = true,
    )
    fun findAllValidButtonsForBroadcast(postId: Long): List<PostButtonEntity>
}
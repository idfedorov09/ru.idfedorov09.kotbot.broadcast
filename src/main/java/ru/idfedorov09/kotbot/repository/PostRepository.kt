package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.idfedorov09.kotbot.domain.entity.PostEntity

interface PostRepository<T: PostEntity> : JpaRepository<T, Long> {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(
        """
            UPDATE post
            SET is_deleted = :isVerified
            WHERE post_id = :postId
        """,
        nativeQuery = true,
    )
    fun updateIsDeleted(postId: Long, isDeleted: Boolean): Int

}
package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.idfedorov09.kotbot.domain.GlobalConstants.POSTS_PAGE_SIZE
import ru.idfedorov09.kotbot.domain.entity.PostEntity

interface PostRepository<T: PostEntity> : JpaRepository<T, Long> {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(
        """
            UPDATE post
            SET is_deleted = :isDeleted
            WHERE post_id = :postId
        """,
        nativeQuery = true,
    )
    fun updateIsDeleted(postId: Long, isDeleted: Boolean): Int

    @Query(
        """
            SELECT p 
            FROM PostEntity p 
            LEFT JOIN FETCH p.buttons b
            WHERE 1 = 1
                AND p.author.id = :postAuthorId
                AND p.isDeleted = false 
                AND p.isBuilt = false
                AND b.isDeleted = false
        """,
        nativeQuery = false
    )
    fun findCurrentPostByAuthorId(postAuthorId: Long): PostEntity?

    @Query(
        """
            SELECT p 
            FROM PostEntity p 
            LEFT JOIN FETCH p.buttons b
            WHERE 1 = 1
                AND p.id = :postId
                AND p.isDeleted = false 
                AND p.isBuilt = true
                AND b.isDeleted = false
        """,
        nativeQuery = false
    )
    fun findAvailablePostById(postId: Long): PostEntity?

    /**
     * pageNum - номер страницы, нумеруется с нуля
     */
    @Query(
        """
            SELECT * FROM post
            WHERE 1 = 1
                AND is_built = true
                AND is_deleted = false
            LIMIT $POSTS_PAGE_SIZE OFFSET :pageNum*$POSTS_PAGE_SIZE
        """,
        nativeQuery = true
    )
    fun findAvailablePostsOnPage(pageNum: Int): List<PostEntity>

    /**
     * Возвращает номер последней страницы, если на страницу приходится $POSTS_PAGE_SIZE страниц
     * -1, если постов нет
     */
    @Query(
        """
            SELECT CASE 
                   WHEN COUNT(*) = 0 THEN -1 
                   ELSE FLOOR((COUNT(*) - 1) / $POSTS_PAGE_SIZE) 
               END 
        FROM post
        WHERE is_built = true
        AND is_deleted = false
        """,
        nativeQuery = true
    )
    fun lastPageNum(): Long
}
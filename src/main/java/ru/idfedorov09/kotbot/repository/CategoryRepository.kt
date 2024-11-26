package ru.idfedorov09.kotbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.idfedorov09.kotbot.domain.GlobalConstants.CATEGORIES_PAGE_SIZE
import ru.idfedorov09.kotbot.domain.entity.CategoryEntity
import ru.idfedorov09.kotbot.domain.entity.PostEntity

interface CategoryRepository<T: CategoryEntity> : JpaRepository<T, Long> {
    fun findByCurrentEditorId(editorId: Long): CategoryEntity?

    /**
     * pageNum - номер страницы, нумеруется с нуля
     */
    @Query(
        """
            SELECT * FROM category
            WHERE 1 = 1
                AND is_built = true
                AND is_deleted = false
            LIMIT $CATEGORIES_PAGE_SIZE OFFSET :pageNum*$CATEGORIES_PAGE_SIZE
        """,
        nativeQuery = true
    )
    fun findAvailableCategoriesOnPage(pageNum: Int): List<PostEntity>

    /**
     * Возвращает номер последней страницы, если на страницу приходится $POSTS_PAGE_SIZE страниц
     * -1, если постов нет
     */
    @Query(
        """
            SELECT CASE 
                   WHEN COUNT(*) = 0 THEN -1 
                   ELSE FLOOR((COUNT(*) - 1) / $CATEGORIES_PAGE_SIZE) 
               END 
            FROM category
            WHERE 1 = 1
                AND is_built = true
                AND is_deleted = false
        """,
        nativeQuery = true
    )
    fun lastPageNum(): Long
}
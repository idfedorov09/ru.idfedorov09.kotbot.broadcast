package ru.idfedorov09.kotbot.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.kotbot.domain.dto.CategoryDTO
import ru.idfedorov09.kotbot.domain.entity.BroadcastDataEntity
import ru.idfedorov09.kotbot.domain.entity.CategoryEntity
import ru.idfedorov09.kotbot.repository.CategoryRepository
import kotlin.jvm.optionals.getOrNull

@Service
class CategoryService {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository<CategoryEntity>

    fun save(categoryEntity: CategoryEntity) = categoryRepository.save(categoryEntity)
    fun save(categoryDTO: CategoryDTO) =
        save(categoryDTO.toEntity()).toDTO()

    fun getCategoryByCurrentEditorId(editorId: Long) =
        categoryRepository.findByCurrentEditorId(editorId)?.toDTO()

    fun getCategoryById(id: Long) =
        categoryRepository.findById(id).getOrNull()?.toDTO()

    open fun findAvailableCategoriesOnPage(pageNum: Int) = categoryRepository.findAvailableCategoriesOnPage(pageNum)
    open fun lastPageNum() = categoryRepository.lastPageNum()
}
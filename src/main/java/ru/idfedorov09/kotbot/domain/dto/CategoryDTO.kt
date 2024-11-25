package ru.idfedorov09.kotbot.domain.dto

import ru.idfedorov09.kotbot.domain.entity.CategoryEntity
import ru.idfedorov09.telegram.bot.base.domain.dto.BaseDTO
import ru.idfedorov09.telegram.bot.base.domain.dto.UserDTO

data class CategoryDTO(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val isSetupByDefault: Boolean = false,
    var currentEditor: UserDTO? = null
): BaseDTO<CategoryEntity>() {
    override fun toEntity() = CategoryEntity(
        id = id,
        title = title,
        description = description,
        isSetupByDefault = isSetupByDefault,
        currentEditor = currentEditor?.toEntity(),
    )
}
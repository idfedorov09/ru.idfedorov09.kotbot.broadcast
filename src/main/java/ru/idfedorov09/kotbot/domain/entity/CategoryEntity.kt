package ru.idfedorov09.kotbot.domain.entity

import jakarta.persistence.*
import ru.idfedorov09.kotbot.domain.dto.CategoryDTO
import ru.idfedorov09.telegram.bot.base.domain.entity.BaseEntity
import ru.idfedorov09.telegram.bot.base.domain.entity.UserEntity

@Entity
@Table(name = "category")
open class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    open var id: Long? = null,
    /** назание категории **/
    @Column(name = "category_title", columnDefinition = "TEXT")
    open var title: String? = null,
    /** описание категории **/
    @Column(name = "description", columnDefinition = "TEXT")
    open var description: String? = null,
    /** установлена ли у пользователей по умолчанию **/
    // TODO: интеграция с регистрацией / авторизацией
    @Column(name = "is_setup_by_default")
    open var isSetupByDefault: Boolean = false,
    /** флаг создания категории **/
    @Column(name = "is_built")
    open var isBuilt: Boolean = false,
    /** удалена ли эта категория **/
    /** true в случае если юзер отменяет создание категории / удаляет ее **/
    @Column(name = "is_deleted")
    open var isDeleted: Boolean = false,
    /** текущий редактор; null если никто не редактирует **/
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "current_editor_id", referencedColumnName = "user_id")
    open var currentEditor: UserEntity? = null,
) : BaseEntity<CategoryDTO>() {
    override fun toDTO() = CategoryDTO(
        id = id,
        title = title,
        description = description,
        isSetupByDefault = isSetupByDefault,
        currentEditor = currentEditor?.toDTO(),
    )
}
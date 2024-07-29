package ru.idfedorov09.kotbot.domain.entity

import jakarta.persistence.*
import ru.idfedorov09.kotbot.domain.dto.PostDTO
import ru.idfedorov09.telegram.bot.base.domain.entity.BaseEntity
import ru.idfedorov09.telegram.bot.base.domain.entity.UserEntity

@Entity
@Table(name = "post")
open class PostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    open var id: Long? = null,
    /** текст сообщения поста **/
    @Column(name = "post_text", columnDefinition = "TEXT")
    open var text: String? = null,
    /** хеш картинок в постах **/
    @Column(name = "post_image_hash", columnDefinition = "TEXT")
    open var imageHash: String? = null,
    /** id создателя поста **/
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_author_id", referencedColumnName = "user_id")
    open var author: UserEntity? = null,
    /** название поста **/
    @Column(name = "post_name", columnDefinition = "TEXT")
    open var name: String? = null,
    /** флаг создания поста **/
    @Column(name = "is_built")
    open var isBuilt: Boolean = false,
    /** последнее сообщение с консолью редактирования (в лс автора) **/
    /** нужно для редактирования поста **/
    @Column(name = "last_console_message_id")
    open var lastConsoleMessageId: Int? = null,
    /** удален ли этот пост **/
    /** true только в случае если юзер отменяет создание рассылки **/
    @Column(name = "is_deleted")
    open var isDeleted: Boolean = false,
    /** Нужно ли web preview при показе поста**/
    @Column(name = "should_show_web_preview")
    open var shouldShowWebPreview: Boolean = false,
    /** Текущий ли это пост **/
    @Column(name = "is_current")
    open var isCurrent: Boolean = false,

    /** кнопки из поста **/
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    open var buttons: MutableList<PostButtonEntity> = mutableListOf()
) : BaseEntity<PostDTO>() {
    override fun toDTO() = PostDTO(
        id = id,
        text = text,
        imageHash = imageHash,
        author = author!!.toDTO(),
        name = name,
        isBuilt = isBuilt,
        lastConsoleMessageId = lastConsoleMessageId,
        isDeleted = isDeleted,
        shouldShowWebPreview = shouldShowWebPreview,
        isCurrent = isCurrent,
        buttons = buttons.map { it.toDTO() }.toMutableList(),
    )
}
package ru.idfedorov09.kotbot.domain.entity

import jakarta.persistence.*
import ru.idfedorov09.kotbot.domain.dto.PostButtonDTO
import ru.idfedorov09.telegram.bot.base.domain.entity.BaseEntity
import ru.idfedorov09.telegram.bot.base.domain.entity.UserEntity
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
@Table(name = "button")
open class PostButtonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "button_id")
    open var id: Long? = null,

    /** текст на кнопке **/
    @Column(name = "button_text", columnDefinition = "TEXT")
    open var text: String? = null,

    /** ссылка в кнопке **/
    @Column(name = "button_link", columnDefinition = "TEXT")
    open var link: String? = null,

    /** коллбэк **/
    @Column(name = "button_callback_data", columnDefinition = "TEXT")
    open var callbackData: String? = null,

    /** id создателя кнопки **/
    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id")
    open var author: UserEntity? = null,

    /** последнее время изменения **/
    @Column(name = "last_modify_dttm")
    open var lastModifyTime: LocalDateTime = LocalDateTime.now(ZoneId.of("Europe/Moscow")),
) : BaseEntity<PostButtonDTO>() {
    override fun toDTO() = PostButtonDTO(
        id = id,
        text = text,
        link = link,
        callbackData = callbackData,
        author = author?.toDTO(),
        lastModifyTime = lastModifyTime,
    )
}
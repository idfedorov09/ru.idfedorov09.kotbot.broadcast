package ru.idfedorov09.kotbot.domain.entity

import jakarta.persistence.*
import ru.idfedorov09.kotbot.domain.dto.BroadcastDataDTO
import ru.idfedorov09.telegram.bot.base.domain.entity.BaseEntity
import ru.idfedorov09.telegram.bot.base.domain.entity.UserEntity

@Entity
@Table(name = "broadcast_data")
open class BroadcastDataEntity(
    @Id
    @Column(name = "user_id")
    open var id: Long? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    open var user: UserEntity = UserEntity(),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "current_post_id", referencedColumnName = "post_id")
    open var currentPost: PostEntity? = null
): BaseEntity<BroadcastDataDTO>() {
    override fun toDTO() = BroadcastDataDTO(
        id = id,
        user = user.toDTO(),
        currentPost = currentPost?.toDTO()
    )
}
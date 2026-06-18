package net.yourein.rebro.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.DoujinBookDetail

data class DoujinDetailWithCircle(
    @Embedded val detail: DoujinBookDetail,
    @Relation(
        parentColumn = "circle_id",
        entityColumn = "id",
    )
    val circle: Circle?,
)

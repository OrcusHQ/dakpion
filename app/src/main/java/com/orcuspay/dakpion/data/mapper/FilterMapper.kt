package com.orcuspay.dakpion.data.mapper

import com.orcuspay.dakpion.data.local.FilterEntity
import com.orcuspay.dakpion.domain.model.Filter

fun Filter.toFilterEntity(): FilterEntity {
    return FilterEntity(
        id = id,
        sender = sender,
        value = value,
        enabled = enabled
    )
}

fun FilterEntity.toFilter(): Filter {
    return Filter(
        id = id,
        sender = sender,
        value = value,
        enabled = enabled
    )
}
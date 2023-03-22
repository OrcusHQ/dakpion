package com.orcuspay.dakpion.data.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.orcuspay.dakpion.domain.model.SMSStatus

@ProvidedTypeConverter
class Converters {
    @TypeConverter
    fun statusToString(value: SMSStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): SMSStatus = SMSStatus.valueOf(value)
}
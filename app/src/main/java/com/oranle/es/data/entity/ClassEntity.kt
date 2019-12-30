package com.oranle.es.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "class")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id : Int = 0,
    @ColumnInfo(name = "class_name") val className : String = "",
    @ColumnInfo(name = "can_register") val isRegister : Boolean = true,
    @ColumnInfo(name = "sheet") val sheet : String = "",
    @ColumnInfo(name = "show_sheet_report") val showSheetReport : String = "",
    @ColumnInfo(name = "member_size") val memberSize : Int = 0
): Serializable {

    @Ignore
    var sheetList = sheet.split(",").toMutableSet()

    @Ignore
    var showSheetReportList = showSheetReport.split(",").toMutableSet()
}
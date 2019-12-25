package com.oranle.es.data.dao

import androidx.room.*
import com.oranle.es.data.entity.SingleChoice

@Dao
interface SingleChoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSingleChoiceBean(singleChoice: SingleChoice)

    @Insert
    suspend fun addSingleChoices(singleChoices: List<SingleChoice>)

    @Query("select * from single_choice where id =:assessmentId")
    suspend fun getSingleChoicesBySheetId(assessmentId: Int): List<SingleChoice>

    @Update
    suspend fun updateSingleChoice(singleChoice: SingleChoice): Int

    @Delete
    suspend fun deleteSingleChoice(singleChoice: SingleChoice): Int

}
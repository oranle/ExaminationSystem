package com.oranle.es.data.dao

import androidx.room.*
import com.oranle.es.data.entity.Role
import com.oranle.es.data.entity.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Query("select * from user where role = :role")
    suspend fun getUsersByRole(role: Int): List<User>

    @Query("select * from user where user_name = :userName and psw = :psw and role = :role")
    suspend fun getUserByAuth(userName: String, psw: String, role: Int): User?

    @Query("select * from user where manager_id = :managerId")
    suspend fun getUserByManagerId(managerId: Int): List<User>

    @Query("select * from user where class_id = :classId")
    suspend fun getUserByClassId(classId: Int): List<User>

    @Query("select * from user where class_id = :classId")
    suspend fun getUserSizeByClassId(classId: Int): List<User>

    @Query("SELECT * FROM user WHERE class_id IN (:classIds) AND role = :role")
    suspend fun getUserByClassIdsAndRole(classIds: List<Int>, role: Int): List<User>

    @Update
    suspend fun updateUser(user: User): Int

    @Delete
    suspend fun deleteUser(user: User): Int

    @Query("delete from user where class_id = :classId and role = :role")
    suspend fun clearExamineeByClassId(classId: Int, role: Int = Role.Examinee.value)

}

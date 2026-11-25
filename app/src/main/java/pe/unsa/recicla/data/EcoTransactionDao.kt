package pe.unsa.recicla.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EcoTransactionDao {
    @Insert suspend fun insert(transaction: EcoTransaction): Long
    @Query("SELECT * FROM eco_transactions ORDER BY createdAt DESC, id DESC") fun observeAll(): Flow<List<EcoTransaction>>
    @Query("SELECT COALESCE(SUM(points), 0) FROM eco_transactions") fun observeBalance(): Flow<Int>
    @Query("SELECT COALESCE(SUM(points), 0) FROM eco_transactions") suspend fun currentBalance(): Int
    @Update suspend fun update(transaction: EcoTransaction)
    @Delete suspend fun delete(transaction: EcoTransaction)
}

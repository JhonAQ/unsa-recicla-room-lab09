@Query("SELECT * FROM eco_transactions ORDER BY createdAt DESC, id DESC") fun observeAll(): Flow<List<EcoTransaction>>
@Query("SELECT COALESCE(SUM(points), 0) FROM eco_transactions") fun observeBalance(): Flow<Int>
@Query("SELECT COALESCE(SUM(points), 0) FROM eco_transactions") suspend fun currentBalance(): Int

class EcoRepository(private val database: AppDatabase) {
    private val dao = database.ecoTransactionDao()

    suspend fun recordReturn(bottles: Int) {
        require(bottles in 1..99)
        dao.insert(EcoTransaction(type = "RETORNO", description = "$bottles botella(s) PET", points = bottles))
    }

    suspend fun redeem(cost: Int, reward: String): Boolean = database.withTransaction {
        require(cost > 0)
        if (dao.currentBalance() < cost) false
        else {
            dao.insert(EcoTransaction(type = "CANJE", description = reward, points = -cost))
            true
        }
    }
}

@Entity(tableName = "eco_transactions")
data class EcoTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val description: String,
    val points: Int,
    val createdAt: Long = System.currentTimeMillis()
)

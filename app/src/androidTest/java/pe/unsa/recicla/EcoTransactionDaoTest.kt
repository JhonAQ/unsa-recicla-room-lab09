package pe.unsa.recicla

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.unsa.recicla.data.AppDatabase
import pe.unsa.recicla.data.EcoRepository
import pe.unsa.recicla.data.EcoTransaction

@RunWith(AndroidJUnit4::class)
class EcoTransactionDaoTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val dao get() = db.ecoTransactionDao()

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }
    @After fun close() = db.close()

    @Test fun insertUpdateDeleteAndBalance() = runBlocking {
        val id = dao.insert(EcoTransaction(type="RETORNO", description="3 botellas PET", points=3))
        assertEquals(3, dao.currentBalance())
        assertEquals("3 botellas PET", dao.observeAll().first().single().description)
        val updated = EcoTransaction(id=id, type="RETORNO", description="4 botellas PET", points=4)
        dao.update(updated)
        assertEquals(4, dao.currentBalance())
        dao.delete(updated)
        assertEquals(0, dao.currentBalance())
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test fun canjeRequiereSaldoYSuMovimientoSeObserva() = runBlocking {
        val repository = EcoRepository(db)
        assertFalse(repository.redeem(5, "Impresiones"))
        assertTrue(dao.observeAll().first().isEmpty())
        repository.recordReturn(7)
        assertTrue(repository.redeem(5, "Impresiones"))
        assertEquals(2, dao.currentBalance())
        assertEquals(listOf(-5, 7), dao.observeAll().first().map { it.points })
    }

    @Test fun datosPersistenAlReabrirLaBase() = runBlocking {
        val name = "room-persistencia-test.db"
        context.deleteDatabase(name)
        val first = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        first.ecoTransactionDao().insert(EcoTransaction(type="RETORNO", description="2 botellas PET", points=2))
        first.close()
        val reopened = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            assertEquals(2, reopened.ecoTransactionDao().currentBalance())
            assertEquals("2 botellas PET", reopened.ecoTransactionDao().observeAll().first().single().description)
        } finally {
            reopened.close()
            context.deleteDatabase(name)
        }
    }
}

package edu.uc.jeong.myplantdiary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import edu.uc.jeong.myplantdiary.ui.main.MainViewModel
import edu.uc.jeong.myplantdiary.ui.main.dto.Plant
import edu.uc.jeong.myplantdiary.ui.main.service.PlantService
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TestRule

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PlantDataUnitTest {
    @get: Rule
    var rule: TestRule = InstantTaskExecutorRule()
    lateinit var mvm: MainViewModel

    var plantService = mockk<PlantService>()

    @Test
    fun confirmEasternRedbud_outputsEasternRedbud() {
        val plant: Plant = Plant("Cercis", "canadensis", "Eastern Redbud")
        assertEquals("Eastern Redbud", plant.toString())
    }

    @Test
    fun searchForRedbud_returnsRedbud() {
        givenAFeedOfMockedPlantDataAreAvailable()
        whenSearchForRedbud()
        thenResultContainsEasternRedbud()
    }

    private fun givenAFeedOfMockedPlantDataAreAvailable() {
        mvm = MainViewModel()
        createMockData()
    }

    private fun createMockData() {
        var allPlantLiveData = MutableLiveData<ArrayList<Plant>>()
        var allPlants = ArrayList<Plant>()
        // create and add plants to our collection.
        var redbud = Plant("Cercis", "canadensis", "Eastern Redbud")
        allPlants.add(redbud)
        var redOak = Plant("Quercus", "rubra", "Red Oak")
        allPlants.add(redOak)
        var whiteOak = Plant("Quercus", "rubra", "White Oak")
        allPlants.add(whiteOak)

        allPlantLiveData.postValue(allPlants)
        every { plantService.fetchPlant(any<String>()) } returns allPlantLiveData
        mvm.plantService = plantService
    }

    private fun whenSearchForRedbud() {
        mvm.fetchPlants("Redbud")
    }

    private fun thenResultContainsEasternRedbud() {
        var redbudFound = false
        mvm. plants.observeForever {
            // here is where we do the observing
            assertNotNull(it)
            assertTrue(it.size > 0)
            it.forEach {
                if (it.genus == "Cercis" && it.species == "canadensis" && it.common.contains("Eastern Redbud")) {
                    redbudFound = true
                }
            }
        }
        assertTrue(redbudFound)
    }

    @Test
    fun searchForGarbage_returnsNothings() {
        givenAFeedOfMockedPlantDataAreAvailable()
        whenSearchForGarbage()
        thenGetZeroResults()
    }

    private fun whenSearchForGarbage() {
        mvm.fetchPlants("fasdfasdfasdfasdf")
    }

    private fun thenGetZeroResults() {
        mvm.plants.observeForever {
            assertEquals(0, it.size)
        }
    }
}

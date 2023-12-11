import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.max

class Day06Part: BehaviorSpec() { init {
    Given("a race") {
        val race = Race(time = 7, distance = 9)
        When("simulating a race and holding the button for 0 ms") {
            Then("it should travel 0") {
                simulateRace(race, 0) shouldBe 0
            }
        }
        When("simulating a race and holding the button for 1 ms") {
            Then("it should travel 6") {
                simulateRace(race, 1) shouldBe 6
            }
        }
        When("simulating a race and holding the button for 3 ms") {
            Then("it should travel 12") {
                simulateRace(race, 3) shouldBe 12
            }
        }
        When("simulating a race and holding the button for 7 ms") {
            Then("it should travel 0") {
                simulateRace(race, 7) shouldBe 0
            }
        }
        When("simulating a race and holding the button for 8 ms") {
            Then("it should travel 0") {
                simulateRace(race, 8) shouldBe 0
            }
        }
        When("finding all winning strategies") {
            Then("it should find all button press times") {
                findWinningStrategies(race) shouldBe setOf(2, 3, 4, 5)
            }
        }
    }
    Given("another race") {
        val race = Race(30, 200)
        Then("finding wining strategies") {
            findWinningStrategies(race).size shouldBe 9
        }
    }
    Given("example") {
        val races = listOf(
            Race(7, 9),
            Race(15, 40),
            Race(30, 200)
        )
        When("finding strategies") {
            val strategiesForRaces = races.map { findWinningStrategies(it) }
            val strategyCountForRaces = strategiesForRaces.map { it.count() }
            Then("strategy counts should be right") {
                strategyCountForRaces shouldBe listOf(4, 8, 9)
            }
            then("multiplying counts should give result") {
                val result = strategyCountForRaces.reduce { acc, i -> acc * i }
                result shouldBe 288
            }
        }
    }
    Given("exercise") {
        val races = listOf(
            Race(54, 239),
            Race(70, 1142),
            Race(82, 1295),
            Race(75, 1253)
        )
        When("finding strategies") {
            val strategiesForRaces = races.map { findWinningStrategies(it) }
            val strategyCountForRaces = strategiesForRaces.map { it.count() }
            then("multiplying counts should give result") {
                val result = strategyCountForRaces.reduce { acc, i -> acc * i }
                result shouldBe 800280
            }
        }
    }
}}

data class Race(val time: Int, val distance: Int)

fun simulateRace(race: Race, buttonPressTime: Int) = max((race.time - buttonPressTime), 0) * buttonPressTime

fun findWinningStrategies(race: Race) = (0..race.time).map {
    time -> time to simulateRace(race, time)
}.filter { it.second > race.distance }.map { it.first }
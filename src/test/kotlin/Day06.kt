import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

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
            val winningStrategies = findWinningStrategies(race)
            winningStrategies shouldBe listOf(11, 12, 13, 14, 15, 16, 17, 18, 19)
            winningStrategies.size shouldBe 9
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

class Day06Part2: BehaviorSpec() { init {
    Given("another race") {
        val race = Race(30, 200)
        Then("finding wining strategies fast by using quadratic formula") {
            val winningStrategies = findWinningStrategiesFast(race)
            winningStrategies shouldBe (11L to 19L)
            winningStrategies.second - winningStrategies.first + 1 shouldBe 9
        }
    }
    Given("exercise part 1") {
        val races = listOf(
            Race(54, 239),
            Race(70, 1142),
            Race(82, 1295),
            Race(75, 1253)
        )
        When("finding strategies using the new fast algorithm based on quadratic formula") {
            val strategiesForRaces = races.map { findWinningStrategiesFast(it) }
            val strategyCountForRaces = strategiesForRaces.map { it.second - it.first + 1 }
            then("multiplying counts should give result") {
                val result = strategyCountForRaces.reduce { acc, i -> acc * i }
                result shouldBe 800280
            }
        }
    }

    Given("exercise") {
        val race = Race(71530, 940200)
        When("finding strategies") {
            val strategiesForRace = findWinningStrategiesFast(race)
            Then("strategy counts should be right") {
                strategiesForRace.second - strategiesForRace.first + 1 shouldBe 71503
            }
        }
    }

    Given("example") {
        val race = Race(54708275L, 239114212951253L)
        When("finding strategies") {
            val strategiesForRace = findWinningStrategiesFast(race)
            Then("strategy counts should be right") {
                strategiesForRace.second - strategiesForRace.first + 1 shouldBe 45128024L
            }
        }
    }

}}

data class Race(val time: Long, val distance: Long)

fun simulateRace(race: Race, buttonPressTime: Long) = max((race.time - buttonPressTime), 0) * buttonPressTime

fun findWinningStrategies(race: Race) = (0..race.time).map {
    time -> time to simulateRace(race, time)
}.filter { it.second > race.distance }.map { it.first }

/**
 * Winning strategies:
 *   (raceTime - buttonPressTime) * buttonPressTime > distance
 *   - buttonPressTime^2 + raceTime * buttonPressTime > distance
 *   buttonPressTime^2 - raceTime * buttonPressTime < -distance
 * Applying quadratic formula
 *   buttonPressTime = raceTime / 2 + sqrt( (raceTime/2)^2 - distance)
 *                     raceTime / 2 - sqrt( (raceTime/2)^2 - distance)
 */
fun findWinningStrategiesFast(race: Race): Pair<Long, Long> {
    val delta = 0.000001
    val h = sqrt( (race.time / 2.0) * (race.time / 2.0) - race.distance)
    val from = race.time / 2.0 - h
    val to = race.time / 2.0 + h
    val fromLong = ceil(from + delta).toLong()
    val toLong = floor(to - delta).toLong()
    return fromLong to toLong
}
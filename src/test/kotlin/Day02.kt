import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe


class Day02Part1: BehaviorSpec() { init {
    val exampleInput = """
        Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
        Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
        Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
        Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
        Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
    """.trimIndent()

    val restriction = mapOf("red" to 12, "green" to 13, "blue" to 14)

    Given("example input") {
        val cubeGames = parseCubeGames(exampleInput)
        Then("should have found right number of games") {
            cubeGames.size shouldBe 5
        }
        Then("first games should be parsed correctly") {
            cubeGames[0].id shouldBe 1
            cubeGames[0].moves shouldBe listOf(
                mapOf("blue" to 3, "red" to 4),
                mapOf("red" to 1, "green" to 2, "blue" to 6),
                mapOf("green" to 2)
            )
        }
        Then("restrictions should be checked") {
            val allowedGames = cubeGames.filter { checkCubeGameRestriction(it, restriction) }.map { it.id }
            allowedGames shouldBe listOf(1, 2, 5)
            allowedGames.sum() shouldBe 8
        }
    }

    Given("exercise input") {
        val cubeGames = parseCubeGames(readResource("inputDay02.txt")!!)
        When("calculating the sum of games fullfilling the restriction") {
            val allowedGames = cubeGames.filter { checkCubeGameRestriction(it, restriction) }.map { it.id }
            val result = allowedGames.sum()
            result shouldBeGreaterThan 2183
            result shouldBe 2447
        }
    }

} }

private fun checkCubeGameRestriction(cubeGame: CubeGame, restriction: Map<String, Int>) = cubeGame.moves.all { cubesWithNr ->
    restriction.entries.all { (color, nr ) ->
        cubesWithNr.getOrDefault(color, 0) <= nr
    }
}

data class CubeGame(val id: Int, val moves: List<Map<String, Int>>)
fun parseCubeGames(input: String) = input.split("\n").map { parseCubeGame(it) }

fun parseCubeGame(line: String): CubeGame {
    val regex = """Game (\d+): (.*)""".toRegex()
    val match = regex.find(line.trim()) ?: throw IllegalArgumentException("Can not parse input=$line")
    if (match.groupValues.size != 3) throw IllegalArgumentException("Wrong number of elements parsed")
    val id = match.groupValues[1].toInt()
    val moves = match.groupValues[2].split(";").map { move ->
        move.split(",").map { cubeWithNrStr ->
                val (nrStr, color) = cubeWithNrStr.trim().split(" ")
                color to nrStr.toInt()
            }.toMap()
    }
    return CubeGame(id, moves)
}
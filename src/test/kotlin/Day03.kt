import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe


val exampleInputDay03 = """
            467..114..
            ...*......
            ..35..633.
            ......#...
            617*......
            .....+.58.
            ..592.....
            ......755.
            ...${'$'}.*....
            .664.598..
        """.trimIndent()

class Day03Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val engineSchematic = parseEngineSchematic(exampleInputDay03)
            Then("should have found right y coords") {
                engineSchematic.size shouldBe 10
            }
            Then("should have found right x coords") {
                engineSchematic.forEach { it.size  shouldBe 10 }
            }
            When("checking for adjacent symbols") {
                checkForAdjacentSymbol(engineSchematic, 0, 0) shouldBe false
                checkForAdjacentSymbol(engineSchematic, 2, 0) shouldBe true
                checkForAdjacentSymbol(engineSchematic, 2, 4) shouldBe true
            }
            When("scanning for part numbers") {
                val numbers = scanPartNumbers(engineSchematic)
                Then("it should have found the first number") {
                    numbers shouldContain 467
                }
                Then("it should have excluded non parts") {
                    numbers shouldNotContain 114
                    numbers shouldNotContain 58
                }
                Then("it should have found the right sum") {
                    numbers.sum() shouldBe 4361
                }
            }
        }
    }

    Given("exercise input") {
        val engineSchematic = parseEngineSchematic(readResource("inputDay03.txt")!!)
        When("finding the parts") {
            val parts = scanPartNumbers(engineSchematic)
            Then("it should have the right sum") {
                parts.sum() shouldBeLessThan 563793
                parts.sum() shouldBeGreaterThan 366076
                parts.sum() shouldBe 531932
            }
        }
    }

} }

class Day03Part2: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val engineSchematic = parseEngineSchematic(exampleInputDay03)
            When("scanning for part numbers and gear") {
                val numbers = scanPartNumbersAndGear(engineSchematic)
                Then("it should have found the first number and gear") {
                    numbers[0].nr shouldBe 467
                    numbers[0].coord shouldBe Coord2(0,0)
                    numbers[0].gears shouldBe setOf(EngineGear('*', Coord2(3, 1)))
                }
                When("pivoting the parts for gears") {
                    val gearsWithNumbers = pivotGears(numbers)
                    Then("it should list by gears") {
                        val parts = gearsWithNumbers[EngineGear('*', Coord2(3, 1))]!!
                        parts.map { it.nr }.toSet() shouldBe setOf(467, 35)
                    }
                    When("filtering wrong gears") {
                        val filteredGears = filterWrongGears(gearsWithNumbers)
                        Then("it only * gears with two number should remain") {
                            filteredGears.keys.map { it.coord }.toSet() shouldBe setOf(Coord2(3, 1), Coord2(5, 8))
                        }
                    }
                    When("calculating gear ration for wrong gears") {
                        val ratios = calculateRatioForWrongGears(gearsWithNumbers)
                        Then("it should have calculated the right ratios") {
                            ratios shouldContainExactly listOf(16345, 451490)
                        }
                        Then("it should have the right sum of ratios") {
                            ratios.sum() shouldBe 467835
                        }
                    }
                }
            }
        }
    }

    Given("exercise input") {
        val engineSchematic = parseEngineSchematic(readResource("inputDay03.txt")!!)
        When("finding wrong gears ratios") {
            val numbers = scanPartNumbersAndGear(engineSchematic)
            Then("it should have the still right sum of numbers") {
                numbers.sumOf { it.nr } shouldBe 531932
            }
            val gearsWithNumbers = pivotGears(numbers)
            val ratios = calculateRatioForWrongGears(gearsWithNumbers)
            Then("it should have found the right sum of ratios") {
                ratios.sum() shouldBe 73646890
            }
        }
    }

} }

fun filterWrongGears(gears: Map<EngineGear, List<EnginePart>>) = gears.filter {
    it.key.c == '*' && it.value.size == 2
}
fun calculateRatioForWrongGears(gears: Map<EngineGear, List<EnginePart>>) = filterWrongGears(gears).map {
    it.value[0].nr * it.value[1].nr
}

fun pivotGears(numbers: List<EnginePart>): Map<EngineGear, List<EnginePart>> =
    numbers.flatMap { number ->
        number.gears.map { gear -> gear to number}
    }.groupBy ({ it.first }, {it.second})


private fun parseEngineSchematic(input: String): EngineSchematic = input.split("\n").map { it.toCharArray().asList() }

private fun scanPartNumbers(engineSchematic: List<List<Char>>): List<Int> {
    val result = mutableListOf<Int>()
    var currentDigits = mutableListOf<Char>()
    var adjacentSymbolFound = false
    fun handleCollectedDigits() {
        if (currentDigits.size > 0) { // was collecting digits for a number and this ended
            if (adjacentSymbolFound) // add only numbers with adjacent symbol
                result += currentDigits.joinToString("").toInt()
            currentDigits = mutableListOf()
            adjacentSymbolFound = false
        }
    }

    for(y in engineSchematic.indices) {
        for (x in 0 until engineSchematic[y].size) {
            val currentChar = engineSchematic[y][x]
            if (currentChar.isDigit()) {
                currentDigits += currentChar
                if (checkForAdjacentSymbol(engineSchematic, x, y)) // During collecting digits must check for adjacent symbols
                    adjacentSymbolFound = true
            } else handleCollectedDigits()
        }
        // handle end of line
        handleCollectedDigits()
    }
    return result
}

private fun scanPartNumbersAndGear(engineSchematic: List<List<Char>>): List<EnginePart> {
    val result = mutableListOf<EnginePart>()
    var currentDigits = mutableListOf<Char>()
    var adjacentGears = mutableSetOf<EngineGear>()
    var numberPos = Coord2(0, 0)
    fun handleCollectedDigits() {
        if (currentDigits.size > 0) { // was collecting digits for a number and this ended
            if (adjacentGears.size > 0) // add only numbers with adjacent symbol
                result += EnginePart(currentDigits.joinToString("").toInt(), numberPos, adjacentGears)
            currentDigits = mutableListOf()
            adjacentGears = mutableSetOf()
        }
    }

    for(y in engineSchematic.indices) {
        for (x in 0 until engineSchematic[y].size) {
            val currentChar = engineSchematic[y][x]
            if (currentChar.isDigit()) {
                if (currentDigits.size == 0) { // Start of a new number
                    numberPos = Coord2(x, y)
                }
                currentDigits += currentChar
                val gears = findAdjacentGears(engineSchematic, x, y)
                if (gears.isNotEmpty()) // During collecting digits must check for adjacent symbols
                    adjacentGears += gears
            } else handleCollectedDigits()
        }
        // handle end of line
        handleCollectedDigits()
    }
    return result
}

data class EnginePart(val nr: Int, val coord: Coord2, val gears: Set<EngineGear>)

data class EngineGear(val c: Char, val coord: Coord2)

private fun checkForAdjacentSymbol(engineSchematic: EngineSchematic, x: Int, y: Int): Boolean {
    for (dx in -1 .. 1)
        for (dy in -1 .. 1)
            if (! (dx == 0 && dy == 0)) {
                val char = engineSchematic.getOrNull(y + dy)?.getOrNull(x + dx)
                if (char != null && char != '.' && !char.isDigit()) return true
            }
    return false
}

private fun findAdjacentGears(engineSchematic: EngineSchematic, x: Int, y: Int): Set<EngineGear> =
    sequence {
        for (dx in -1 .. 1)
            for (dy in -1 .. 1)
                if (! (dx == 0 && dy == 0)) {
                    val char = engineSchematic.getOrNull(y + dy)?.getOrNull(x + dx)
                    if (char != null && char != '.' && !char.isDigit())
                        yield(EngineGear(char, Coord2(x + dx, y + dy)))
                }
    }.toSet()

typealias EngineSchematic = List<List<Char>>

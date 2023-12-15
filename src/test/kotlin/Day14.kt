import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe

val exampleInputDay14 = """
        O....#....
        O.OO#....#
        .....##...
        OO.#O....O
        .O.....O#.
        O.#..O.#.#
        ..O..#O..O
        .......O..
        #....###..
        #OO..#....
    """.trimIndent()

class Day14Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val reflectorMap = parseReflectorMap(exampleInputDay14)
            Then("reflector map should be parsed mirror maps correctly") {
                reflectorMap.size shouldBe 10
                reflectorMap[0].size shouldBe 10
                reflectorMap[0][0] shouldBe 'O'
                reflectorMap[0][5] shouldBe '#'
                reflectorMap[9][9] shouldBe '.'
            }
            Then("it should be converted correctly back to string") {
                reflectorMap.toStringReflectorMap() shouldBe exampleInputDay14
            }
            When("tilting reflector map") {
                val tiltedReflectorMap = tiltNorthReflectorMap(reflectorMap)
                Then("it should move reflectors to the north") {
                    tiltedReflectorMap.toStringReflectorMap() shouldBe """
                    OOOO.#.O..
                    OO..#....#
                    OO..O##..O
                    O..#.OO...
                    ........#.
                    ..#....#.#
                    ..O..#.O.O
                    ..O.......
                    #....###..
                    #....#....
                """.trimIndent()
                }
                Then("calculating load should have the right result") {
                    calculateLoad(tiltedReflectorMap) shouldBe 136
                }
            }
        }
    }

    Given("exercise input") {
        val reflectorMap = parseReflectorMap(readResource("inputDay14.txt")!!)
        Then("tilting and calculating load should have the right result") {
            calculateLoad(tiltNorthReflectorMap(reflectorMap)) shouldBe 105461
        }
    }
} }

class Day14Part2: BehaviorSpec() { init {

    Given("example input") {
        val reflectorMap = parseReflectorMap(exampleInputDay14)
        When("tilting reflector map in 4 directions") {
            val tiltedReflectorMap = tiltEastReflectorMap(
                tiltSouthReflectorMap(
                    tiltWestReflectorMap(
                        tiltNorthReflectorMap(reflectorMap))))
            Then("it should move reflectors to the north") {
                tiltedReflectorMap.toStringReflectorMap() shouldBe """
                    .....#....
                    ....#...O#
                    ...OO##...
                    .OO#......
                    .....OOO#.
                    .O#...O#.#
                    ....O#....
                    ......OOOO
                    #...O###..
                    #..OO#....
                """.trimIndent()
            }
        }
        Then("executing one cycle should have turned it in all directions") {
            executeOneTiltCycle(reflectorMap).toStringReflectorMap() shouldBe """
                    .....#....
                    ....#...O#
                    ...OO##...
                    .OO#......
                    .....OOO#.
                    .O#...O#.#
                    ....O#....
                    ......OOOO
                    #...O###..
                    #..OO#....
                """.trimIndent()
        }
        Then("executing three cycles should have expected result") {
            cycleManyTimes(reflectorMap, 3).toStringReflectorMap() shouldBe """
                    .....#....
                    ....#...O#
                    .....##...
                    ..O#......
                    .....OOO#.
                    .O#...O#.#
                    ....O#...O
                    .......OOO
                    #...O###.O
                    #.OOO#...O
                """.trimIndent()
        }

        When("looking for a repeated map") {
            val (cycleNr, _) = cycleUntilRepeatedMap(reflectorMap)
            Then("it should have found a cycle before 1000000000") {
                cycleNr shouldBeLessThan 1000000000 // it should stop after a cycle
            }
        }
        When("executing many cycles non-optimized and optimized for cycle count 20") {
            val result1 = cycleManyTimes(reflectorMap, 20)
            val result2 = cycleManyTimesOptimized(reflectorMap, 20)
            Then("both versions should have the same result") {
                result1 shouldBe result2
            }
        }
        When("executing many cycles non-optimized and optimized for cycle count 30") {
            val result1 = cycleManyTimes(reflectorMap, 30)
            val result2 = cycleManyTimesOptimized(reflectorMap, 30)
            Then("both versions should have the same result") {
                result1 shouldBe result2
            }
        }
        When("executing many cycles non-optimized and optimized for cycle count 1000") {
            val result1 = cycleManyTimes(reflectorMap, 1000)
            val result2 = cycleManyTimesOptimized(reflectorMap, 1000)
            Then("both versions should have the same result") {
                result1 shouldBe result2
            }
        }
        When("executing many cycles optimized") {
            val result = cycleManyTimesOptimized(reflectorMap, 1000000000)
            Then("the result should have the right load") {
                calculateLoad(result) shouldBe 64
            }
        }
    }

    Given("exercise input") {
        val reflectorMap = parseReflectorMap(readResource("inputDay14.txt")!!)
        Then("executing many cycles and calculating load should have the right result") {
            val result = cycleManyTimesOptimized(reflectorMap, 1000000000)
            calculateLoad(result) shouldBe 102829
        }
    }
} }

fun parseReflectorMap(input: String) = input.split("\n").map { it.trim().toCharArray().toList() }

fun List<List<Char>>.toStringReflectorMap() = this.joinToString("\n") { it.joinToString("") }

fun tiltNorthReflectorMap(reflectorMap: List<List<Char>>): MutableList<MutableList<Char>> {
    val resultMap = MutableList(reflectorMap.size) { y ->
        val row = reflectorMap[y]
        MutableList(row.size) {x ->
            row[x]
        }
    }

    fun moveReflectorsInColumn(x: Int) {
        for (y1 in 1..< resultMap.size) {
            if (resultMap[y1][x] == 'O') {
                // Move up
                for (y2 in y1 - 1 downTo 0)
                    if (resultMap[y2][x] == '.') {
                        resultMap[y2+1][x] = '.'
                        resultMap[y2][x] = 'O'
                    }
                    else break
            }
        }
    }
    for (x in resultMap[0].indices) moveReflectorsInColumn(x)
    return resultMap
}
fun tiltSouthReflectorMap(reflectorMap: List<List<Char>>): MutableList<MutableList<Char>> {
    val resultMap = MutableList(reflectorMap.size) { y ->
        val row = reflectorMap[y]
        MutableList(row.size) {x ->
            row[x]
        }
    }

    fun moveReflectorsInColumn(x: Int) {
        for (y1 in resultMap.size - 2 downTo 0) {
            if (resultMap[y1][x] == 'O') {
                // Move up
                for (y2 in y1 + 1..< resultMap.size)
                    if (resultMap[y2][x] == '.') {
                        resultMap[y2-1][x] = '.'
                        resultMap[y2][x] = 'O'
                    }
                    else break
            }
        }
    }
    for (x in resultMap[0].indices) moveReflectorsInColumn(x)
    return resultMap
}
fun tiltEastReflectorMap(reflectorMap: List<List<Char>>): MutableList<MutableList<Char>> {
    val resultMap = MutableList(reflectorMap.size) { y ->
        val row = reflectorMap[y]
        MutableList(row.size) {x ->
            row[x]
        }
    }

    fun moveReflectorsInColumn(y: Int) {
        for (x1 in resultMap[y].size - 2 downTo 0) {
            if (resultMap[y][x1] == 'O') {
                // Move up
                for (x2 in x1 + 1..< resultMap[y].size)
                    if (resultMap[y][x2] == '.') {
                        resultMap[y][x2-1] = '.'
                        resultMap[y][x2] = 'O'
                    }
                    else break
            }
        }
    }
    for (y in resultMap.indices) moveReflectorsInColumn(y)
    return resultMap
}
fun tiltWestReflectorMap(reflectorMap: List<List<Char>>): MutableList<MutableList<Char>> {
    val resultMap = MutableList(reflectorMap.size) { y ->
        val row = reflectorMap[y]
        MutableList(row.size) {x ->
            row[x]
        }
    }

    fun moveReflectorsInRow(y: Int) {
        for (x1 in 1..< resultMap[y].size) {
            if (resultMap[y][x1] == 'O') {
                // Move up
                for (x2 in x1 - 1 downTo 0)
                    if (resultMap[y][x2] == '.') {
                        resultMap[y][x2+1] = '.'
                        resultMap[y][x2] = 'O'
                    }
                    else break
            }
        }
    }
    for (y in resultMap.indices) moveReflectorsInRow(y)
    return resultMap
}

fun calculateLoad(reflectorMap: List<List<Char>>): Int {
    var result = 0
    for (y in reflectorMap.indices)
        for (x in reflectorMap[y].indices)
            if (reflectorMap[y][x] == 'O')
                result += reflectorMap.size - y
    return result
}

fun executeOneTiltCycle(reflectorMap: List<List<Char>>) = tiltEastReflectorMap(
    tiltSouthReflectorMap(
        tiltWestReflectorMap(
            tiltNorthReflectorMap(reflectorMap))))

fun cycleUntilRepeatedMap(reflectorMap: List<List<Char>>): Triple<Int, Int, List<List<Char>>> {
    var currentReflectorMap = reflectorMap
    var cycleNr = 0
    val mapHashMap = mutableMapOf(reflectorMap to 0)
    for (i in 1..1000) {
        val tiltedReflectorMap = executeOneTiltCycle(currentReflectorMap)
        val found = mapHashMap[tiltedReflectorMap] // cycle start
        if (found != null) return Triple(found, cycleNr - found + 1, currentReflectorMap)
        cycleNr++
        currentReflectorMap = tiltedReflectorMap
        mapHashMap[currentReflectorMap] = cycleNr
    }
    throw InternalError("No cycle found")
}
fun cycleManyTimes(reflectorMap: List<List<Char>>, cycleCount: Int): List<List<Char>> {
    var currentReflectorMap = reflectorMap
    for (i in 1..cycleCount) {
        currentReflectorMap = executeOneTiltCycle(currentReflectorMap)
    }
    return currentReflectorMap
}
fun cycleManyTimesOptimized(reflectorMap: List<List<Char>>, cycleCount: Int): List<List<Char>> {
    val (cycleStart, cycleLength, interimMap) = cycleUntilRepeatedMap(reflectorMap)
    val remaining = (cycleCount - cycleStart + 1) % cycleLength
    var currentReflectorMap = interimMap
    for (i in 1..remaining) {
        currentReflectorMap = executeOneTiltCycle(currentReflectorMap)
    }
    return currentReflectorMap
}
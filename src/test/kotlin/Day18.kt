import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


val exampleInputDay18 = """
    R 6 (#70c710)
    D 5 (#0dc571)
    L 2 (#5713f0)
    D 2 (#d2c081)
    R 2 (#59c680)
    D 2 (#411b91)
    L 5 (#8ceee2)
    U 2 (#caa173)
    L 1 (#1b58a2)
    U 2 (#caa171)
    R 2 (#7807d2)
    U 3 (#a77fa3)
    L 2 (#015232)
    U 2 (#7a21e3)
""".trimIndent()

class Day18Part1: BehaviorSpec() { init {

    Given("dig plan") {
        When("parsing input") {
            val digPlan = parseDigPlan(exampleInputDay18)
            Then("it should have been parsed correctly") {
                digPlan.size shouldBe 14
                digPlan[0] shouldBe DigStep(DigDirection.RIGHT, 6)
            }
            When("executing example input and drawing map") {
                val map = executePlan(digPlan)
                val mapString = drawDigMap(map)
                Then("map should look right") {
                    mapString shouldBe """
                        #######
                        #.....#
                        ###...#
                        ..#...#
                        ..#...#
                        ###.###
                        #...#..
                        ##..###
                        .#....#
                        .######""".trimIndent()
                }
                When("finding a point in the interior") {
                    val interiorCoord = findInterior(map)
                    Then("it should have found the coordinates") {
                        (interiorCoord!! !in map) shouldBe true // should not be the trench
                        interiorCoord.neighbors().any { it in map } shouldBe true // close to a trench
                        interiorCoord.neighbors().any { it !in map } shouldBe true // at least on neighbour to dig
                    }
                }
                When("filling the interior") {
                    val filledMap = fillInterior(map)
                    Then("it should be nicely filled") {
                        drawDigMap(filledMap) shouldBe """
                            #######
                            #######
                            #######
                            ..#####
                            ..#####
                            #######
                            #####..
                            #######
                            .######
                            .######""".trimIndent()
                    }
                    Then("it should have filled all interior cubic meters") {
                        filledMap.size shouldBe 62
                    }
                }

            }
        }
    }

    Given("exercise input") {
        val exerciseInput = readResource("inputDay18.txt")!!
        val digPlan = parseDigPlan(exerciseInput)
        When("executing example input and drawing map") {
            val map = executePlan(digPlan)
            //println(drawDigMap(map)); repeat(2) { println() }
            val filledMap = fillInterior(map)
            //println(drawDigMap(filledMap)); println()
            Then("filled map size is the solution") {
                filledMap.size shouldBe 53300
            }
        }
    }
}}

fun parseDigPlan(input: String) = input.split("\n").map {
    parseDigStep(it.trim())
}

fun parseDigStep(line: String): DigStep {
    val regex = """([RLUD]) (\d+) \(#([0-9a-f]+)\)""".toRegex()
    val (dirStr, nrStr, _) = regex
        .matchEntire(line)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect input line $line")
    val dir = when(dirStr) {
        "R" -> DigDirection.RIGHT
        "L" -> DigDirection.LEFT
        "U" -> DigDirection.UP
        "D" -> DigDirection.DOWN
        else -> throw IllegalArgumentException("Unknown direction $dirStr")
    }
    val nr = nrStr.toInt()
    return DigStep(dir, nr)

}

fun executePlan(digPlan: List<DigStep>) = buildSet {
    var pos = Coord2(0, 0)
    for (digStep in digPlan) {
        when(digStep.dir) {
            DigDirection.UP ->
                for (i in 1..digStep.nr) {
                    add(pos)
                    pos = Coord2(pos.x - 1, pos.y)
                }
            DigDirection.DOWN ->
                for (i in 1..digStep.nr) {
                    add(pos)
                    pos = Coord2(pos.x + 1, pos.y)
                }
            DigDirection.RIGHT ->
                for (i in 1..digStep.nr) {
                    add(pos)
                    pos = Coord2(pos.x, pos.y + 1)
                }
            DigDirection.LEFT ->
                for (i in 1..digStep.nr) {
                    add(pos)
                    pos = Coord2(pos.x, pos.y - 1)
                }
        }
    }
}

fun drawDigMap(set: Set<Coord2>): String {
    val (minX, maxX) = minMaxX(set)
    val (minY, maxY) = minMaxY(set)
    val mapList = buildList {
        for (x in minX .. maxX)
            add(buildList {
                for (y in minY..maxY) {
                    val c = if (Coord2(x, y) in set) '#'
                    else '.'
                    add(c)
                }
            })
    }
    return mapList.joinToString("\n") { it.joinToString("")}
}

fun minMaxX(set: Set<Coord2>) = Pair(
    set.map { it.x }.minBy { it },
    set.map { it.x }.maxBy { it }
    )

fun minMaxY(set: Set<Coord2>) = Pair(
    set.map { it.y }.minBy { it },
    set.map { it.y }.maxBy { it }
)

fun findInterior(set: Set<Coord2>): Coord2? {
    val (minX, maxX) = minMaxX(set)
    val (minY, maxY) = minMaxY(set)
    for (y in minY..maxY) {
        var singleTrenchFound = false
        var consecutiveTrenchesFound = false
        for (x in minX .. maxX)
            if (Coord2(x, y) !in set) {
                if (singleTrenchFound && !consecutiveTrenchesFound)  return Coord2(x, y)
                singleTrenchFound = false
                consecutiveTrenchesFound = false
            } else {
                if (singleTrenchFound) consecutiveTrenchesFound = true
                singleTrenchFound = true
            }
    }
    return null
}

fun fillInterior(set: Set<Coord2>): MutableSet<Coord2> {
    val filledSet = set.toMutableSet()
    val interior = findInterior(set)!!
    var currentSet = setOf(interior)
    filledSet.add(interior)
    while (currentSet.isNotEmpty()) {
        val nextSet = mutableSetOf<Coord2>()
        for (current in currentSet) {
            val neighborsToDig = current.neighbors().filter { it !in filledSet }
            nextSet.addAll(neighborsToDig)
            filledSet += neighborsToDig
        }
        currentSet = nextSet
    }
    return filledSet
}

enum class DigDirection { RIGHT, LEFT, UP, DOWN }

data class DigStep(val dir: DigDirection, val nr: Int)


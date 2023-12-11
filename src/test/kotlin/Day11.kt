import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs


val exampleInputDay11 = """
        ...#......
        .......#..
        #.........
        ..........
        ......#...
        .#........
        .........#
        ..........
        .......#..
        #...#.....
    """.trimIndent()

class Day11Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val galaxyMap = parseGalaxyMap(exampleInputDay11)
            Then("map should be parsed correctly") {
                galaxyMap.size shouldBe 10
                galaxyMap[0].size shouldBe 10
                galaxyMap[0][3] shouldBe '#'
                galaxyMap[9][9] shouldBe '.'
            }
            When("duplicating empty rows and columns") {
                val expandedGalaxyMap = duplicateEmptyRowsAndColumns(galaxyMap)
                Then("map should be expanded correctly") {
                    expandedGalaxyMap shouldBe parseGalaxyMap("""
                        ....#........
                        .........#...
                        #............
                        .............
                        .............
                        ........#....
                        .#...........
                        ............#
                        .............
                        .............
                        .........#...
                        #....#.......
                    """.trimIndent())
                }
                When("finding galaxy coordinates in the galaxy map") {
                    val galaxies = findGalaxyCoordinates(expandedGalaxyMap)
                    Then("it should have found the right galaxies") {
                        galaxies.size shouldBe 9
                        galaxies[8] shouldBe Coord2(5, 11)
                    }
                    When("create all galaxy pairs") {
                        val dists = createPairs(galaxies).map { it.first.manhattanDistance(it.second)}
                        Then("it should have found all pairs") {
                            dists.sum() shouldBe 374
                        }
                    }
                }
            }
        }
    }

    Given("exercise input") {
        val galaxyMap = parseGalaxyMap(readResource("inputDay11.txt")!!)
        When("expanding, finding galaxies, calculating distances and summing") {
            val expandedGalaxyMap = duplicateEmptyRowsAndColumns(galaxyMap)
            val galaxies = findGalaxyCoordinates(expandedGalaxyMap)
            val dists = createPairs(galaxies).map { it.first.manhattanDistance(it.second)}
            Then("it should have calculated the right sum") {
                dists.sum() shouldBe 9509330
            }
        }
    }
} }

class Day11Part2: BehaviorSpec() { init {

    Given("example input") {
        When("parsing, finding coodrinates, finding rows and columns to expand") {
            val galaxyMap = parseGalaxyMap(exampleInputDay11)
            val galaxies = findGalaxyCoordinates(galaxyMap)
            val emptyRows = findEmptyRows(galaxyMap)
            val emptyColumns = findEmptyColumns(galaxyMap)
            Then("it should have found the right empty rows and columns") {
                emptyRows.size shouldBe 2
                emptyRows[0] shouldBe 3
                emptyColumns.size shouldBe 3
                emptyColumns[1] shouldBe 5
            }
            When("finding all paris and summing all distances with expansion of 2") {
                val dists = createPairs(galaxies).map { manhattanDistanceInExpandingUniverse(it.first, it.second, emptyColumns, emptyRows, 2) }
                Then("it should have found all pairs and calculated the right distances and sum") {
                    dists.sum() shouldBe 374
                }
            }
            When("finding all paris and summing all distances with expansion of 10") {
                val dists = createPairs(galaxies).map { manhattanDistanceInExpandingUniverse(it.first, it.second, emptyColumns, emptyRows, 10) }
                Then("it should have found all pairs and calculated the right distances and sum") {
                    dists.sum() shouldBe 1030L
                }
            }
            When("finding all paris and summing all distances with expansion of 100") {
                val dists = createPairs(galaxies).map { manhattanDistanceInExpandingUniverse(it.first, it.second, emptyColumns, emptyRows, 100) }
                Then("it should have found all pairs and calculated the right distances and sum") {
                    dists.sum() shouldBe 8410L
                }
            }
        }
    }

    Given("manhattan distance in an expanded univers") {
        When("calculating manhattan distance without expansion") {
            manhattanDistanceInExpandingUniverse(Coord2(0, 2), Coord2(3, 0), listOf(), listOf(), 2) shouldBe 5
        }
        When("calculating manhattan distance with column expansion") {
            manhattanDistanceInExpandingUniverse(Coord2(0, 2), Coord2(3, 0), listOf(1), listOf(), 2) shouldBe 6
        }
        When("calculating manhattan distance with row expansion") {
            manhattanDistanceInExpandingUniverse(Coord2(0, 2), Coord2(3, 0), listOf(), listOf(1, 2), 2) shouldBe 7
        }
    }

    Given("exercise input") {
        val galaxyMap = parseGalaxyMap(readResource("inputDay11.txt")!!)
        When("finding galaxies, calculating distances with expansion and summing") {
            val galaxies = findGalaxyCoordinates(galaxyMap)
            val emptyRows = findEmptyRows(galaxyMap)
            val emptyColumns = findEmptyColumns(galaxyMap)
            val dists = createPairs(galaxies).map { manhattanDistanceInExpandingUniverse(it.first, it.second, emptyColumns, emptyRows, 1000_000) }
            Then("it should have calculated the right sum") {
                dists.sum() shouldBe 635832237682L
            }
        }
    }
} }

fun parseGalaxyMap(input: String) = input.split("\n").map {
    it.trim().toCharArray().toList()
}

fun duplicateEmptyRowsAndColumns(galaxyMap: List<List<Char>>): List<List<Char>> {
    val emptyColumns = buildList {
        for (x in galaxyMap[0].indices) {
            if (galaxyMap.indices.all { galaxyMap[it][x] == '.'})
                add(x)
        }
    }.toSet()
    val duplicatedColumns = buildList {
        for (row in galaxyMap) {
            add(buildList {
              for ((x, c) in row.withIndex()) {
                  if (x in emptyColumns) {
                      add(c); add(c) // duplicate
                  } else add(c)
              }
            })
        }
    }
    return buildList { // duplicate rows
        for (row in duplicatedColumns)
            if (row.all { it == '.'}) {
                add(row); add(row) // duplicate
            }
            else add(row)
    }
}

fun findGalaxyCoordinates(galaxyMap: List<List<Char>>) =
    buildList {
        for ((y, row) in galaxyMap.withIndex()) {
            for ((x, c) in row.withIndex()) {
                if (c == '#') add(Coord2(x, y))
            }
        }
    }

fun createPairs(galaxies: List<Coord2>): List<Pair<Coord2, Coord2>> =
    buildList {
        val first = galaxies.first()
        for (other in galaxies.drop(1)) add(first to other)
    } +  if (galaxies.size > 1) createPairs(galaxies.drop(1)) else emptyList()

fun findEmptyRows(galaxyMap: List<List<Char>>) = buildList {
    for ((y, row) in galaxyMap.withIndex()) {
        if (row.all { it == '.'}) add(y)
    }
}

fun findEmptyColumns(galaxyMap: List<List<Char>>) = buildList {
    for (x in galaxyMap[0].indices) {
        if (galaxyMap.indices.all { galaxyMap[it][x] == '.' })
            add(x)
    }
}

fun manhattanDistanceInExpandingUniverse(from: Coord2, to: Coord2, emptyColumns: List<Int>, emptyRows: List<Int>, expansion: Int): Long {
    fun countInRange(ints: List<Int>, from: Int, to: Int): Long {
        val min = min(from, to)
        val max = max(from, to)
        return ints.count { it in min..max }.toLong()
    }
    return abs(from.x - to.x) + abs(from.y - to.y) +
            countInRange(emptyRows, from.y, to.y) * (expansion - 1) + countInRange(emptyColumns, from.x, to.x) * (expansion - 1)
}

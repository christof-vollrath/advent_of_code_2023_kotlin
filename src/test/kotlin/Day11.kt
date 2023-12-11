import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


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
                When("finding galaxy coordinates in the galaxy amp") {
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

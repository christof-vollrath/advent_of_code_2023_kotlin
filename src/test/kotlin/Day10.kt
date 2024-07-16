import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

val exampleInput1Day10 = """
    .....
    .S-7.
    .|.|.
    .L-J.
    .....
    """.trimIndent()
val exampleInput2Day10 = """
    -L|F7
    7S-7|
    L|7||
    -L-J|
    L|-JF
    """.trimIndent()
val exampleInput3Day10 = """
    7-F7-
    .FJ|7
    SJLL7
    |F--J
    LJ.LJ
    """.trimIndent()

class Day10Part1: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input 1") {
            val maze1 = parseMaze(exampleInput1Day10)
            Then("maze should be parsed correctly") {
                maze1.size shouldBe 5
                maze1[1].toCharArray() shouldBe ".S-7.".toCharArray()
            }
            When("searching for the start") {
                val start = findStart(maze1)
                Then("it should find it") {
                    start shouldBe Coord2(1, 1)
                }
            }
            When("following the loop from start") {
                val loop = followLoop(maze1)
                Then("loop should go around") {
                    loop.map { it.second }.joinToString("") shouldBe "S-7|J-L|"
                }
            }
        }
        When("parsing the input 2") {
            val maze2 = parseMaze(exampleInput2Day10)
            Then("maze should be parsed correctly") {
                maze2.size shouldBe 5
                maze2[1].toCharArray() shouldBe "7S-7|".toCharArray()
            }
            When("searching for the start") {
                val start = findStart(maze2)
                Then("it should find it") {
                    start shouldBe Coord2(1, 1)
                }
            }
            When("following the loop from start") {
                val loop = followLoop(maze2)
                Then("loop should go around") {
                    loop.map { it.second }.joinToString("") shouldBe "S-7|J-L|"
                }
            }
        }
        When("parsing the input 3") {
            val maze2 = parseMaze(exampleInput3Day10)
            Then("maze should be parsed correctly") {
                maze2.size shouldBe 5
                maze2[1].toCharArray() shouldBe ".FJ|7".toCharArray()
            }
            When("searching for the start") {
                val start = findStart(maze2)
                Then("it should find it") {
                    start shouldBe Coord2(0, 2)
                }
            }
            When("following the loop from start") {
                val loop = followLoop(maze2)
                Then("loop should go around") {
                    loop.map { it.second }.joinToString("") shouldBe "SJFJF7|L7J--FJL|"
                }
            }
        }
    }
    Given("exercise input") {
        val maze = parseMaze(readResource("inputDay10.txt")!!)
        maze.size shouldBe 140
        When("following the loop from start") {
            val loop = followLoop(maze)
            Then("loop should go around") {
                loop.size / 2 shouldBe 6907
            }
        }
    }
} }

class Day10Part2: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the example 1 part 2") {
            val example1part2 = """
            ...........
            .S-------7.
            .|F-----7|.
            .||.....||.
            .||.....||.
            .|L-7.F-J|.
            .|..|.|..|.
            .L--J.L--J.
            ...........
            """.trimIndent()
            val maze = parseMaze(example1part2)
            Then("maze should be parsed correctly") {
                maze.size shouldBe 9
                maze[1].toCharArray() shouldBe ".S-------7.".toCharArray()
            }
            When("searching for the start") {
                val start = findStart(maze)
                Then("it should find it") {
                    start shouldBe Coord2(1, 1)
                }
            }
            When("following the loop from start") {
                val loop = followLoop(maze)
                Then("loop should go around") {
                    loop.map { it.second }.joinToString("") shouldStartWith  "S-------7||||"
                }
                When("counting inside tiles") {
                    val count = countInsideTiles(loop, maze)
                    Then("it should find 4 inside tiles") {
                        count shouldBe 4
                    }
                }
            }
        }
        When("counting the example 2 part 2") {
            val example2part2 = """
            .F----7F7F7F7F-7....
            .|F--7||||||||FJ....
            .||.FJ||||||||L7....
            FJL7L7LJLJ||LJ.L-7..
            L--J.L7...LJS7F-7L7.
            ....F-J..F7FJ|L7L7L7
            ....L7.F7||L7|.L7L7|
            .....|FJLJ|FJ|F7|.LJ
            ....FJL-7.||.||||...
            ....L---J.LJ.LJLJ...
            """.trimIndent()
            val maze = parseMaze(example2part2)
            maze.size shouldBe 10
            val loop = followLoop(maze)
            val count = countInsideTiles(loop, maze)
            Then("it should count inside tiles") {
                count shouldBe 8
            }
        }
        When("counting the example 3 part 2") {
            val example2part2 = """
            FF7FSF7F7F7F7F7F---7
            L|LJ||||||||||||F--J
            FL-7LJLJ||||||LJL-77
            F--JF--7||LJLJ7F7FJ-
            L---JF-JLJ.||-FJLJJ7
            |F|F-JF---7F7-L7L|7|
            |FFJF7L7F-JF7|JL---7
            7-L-JL7||F7|L7F-7F7|
            L.L7LFJ|||||FJL7||LJ
            L7JLJL-JLJLJL--JLJ.L
            """.trimIndent()
            val maze = parseMaze(example2part2)
            maze.size shouldBe 10
            val loop = followLoop(maze)
            val count = countInsideTiles(loop, maze)
            Then("it should count inside tiles") {
                count shouldBe 10
            }
        }
    }

    Given("exercise input") {
        val maze = parseMaze(readResource("inputDay10.txt")!!)
        maze.size shouldBe 140
        When("following the loop from start and count inside tiles") {
            val loop = followLoop(maze)
            val count = countInsideTiles(loop, maze)
            Then("it should count inside tiles") {
                count shouldBe 541
            }
        }
    }
} }

typealias Maze = List<List<Char>>

fun parseMaze(input: String): Maze = input.split("\n").map { it.trim().toCharArray().toList() }

fun findStart(maze: Maze): Coord2 {
    for (y in maze.indices) {
        for (x in maze[y].indices )
            if (maze[y][x] == 'S') return Coord2(x, y)
    }
    throw IllegalArgumentException("No start found")
}

fun followLoop(maze: Maze) =
    sequence {
        val startCoord = findStart(maze)
        yield(startCoord to maze[startCoord] )
        val startConnections = getConnections('S', startCoord, maze)
        var prevCoord = startCoord
        var currCoord = startConnections[0]
        while(true) {
            val curr = maze[currCoord]
            if (curr == 'S') break
            yield(currCoord to curr)
            val nextConnections = getConnections(curr, currCoord, maze)
            val nextCoord = nextConnections.first { it != prevCoord }
            prevCoord = currCoord
            currCoord = nextCoord
        }
    }.toList()


fun getConnections(tile: Char, coord: Coord2, maze: Maze): List<Coord2> {
    val connectionOffsets = getConnectionOffsetsWithStart(tile, coord, maze)
    return connectionOffsets.map { coord + it }.filter { maze[it] != '.' }
}

fun getConnectionOffsets(tile: Char): List<Coord2> = when(tile) {
    '|' -> listOf(Coord2(0, -1), Coord2(0, 1))
    '-' -> listOf(Coord2(-1, 0), Coord2(1, 0))
    'L' -> listOf(Coord2(0, -1), Coord2(1, 0))
    'J' -> listOf(Coord2(0, -1), Coord2(-1, 0))
    '7' -> listOf(Coord2(-1, 0), Coord2(0, 1))
    'F' -> listOf(Coord2(1, 0), Coord2(0, 1))
    '.' -> listOf()
    else -> throw IllegalArgumentException("Unkown tile $tile")
}

fun getConnectionOffsetsWithStart(tile: Char, coord: Coord2, maze: Maze): List<Coord2> = if (tile == 'S') getConnectionOffsets(replaceStart(coord, maze))
    else getConnectionOffsets(tile)



fun replaceStart(coord: Coord2, maze: Maze): Char = // find what the start tile really is
    when {
        coord.y > 0 && getConnectionOffsets(maze[coord + Coord2(0, -1)]).contains(Coord2(0, 1)) -> {
            when {
                coord.x < maze[coord.y].size && getConnectionOffsets(maze[coord + Coord2(1, 0)]).contains(Coord2(-1, 0)) -> 'L'
                coord.y < maze.size && getConnectionOffsets(maze[coord + Coord2(0, 1)]).contains(Coord2(0, -1)) -> '|'
                coord.x > 0 && getConnectionOffsets(maze[coord + Coord2(-1, 0)]).contains(Coord2(1, 0)) -> 'J'
                else -> throw IllegalArgumentException("Cannot replace start at $coord")
            }
        }
        coord.x < maze[coord.y].size && getConnectionOffsets(maze[coord + Coord2(1, 0)]).contains(Coord2(-1, 0)) -> {
            when {
                coord.y < maze.size && getConnectionOffsets(maze[coord + Coord2(0, 1)]).contains(Coord2(0, -1)) -> 'F'
                coord.x > 0 && getConnectionOffsets(maze[coord + Coord2(-1, 0)]).contains(Coord2(1, 0)) -> '-'
                else -> throw IllegalArgumentException("Cannot replace start at $coord")
            }
        }
        coord.y < maze.size && getConnectionOffsets(maze[coord + Coord2(0, 1)]).contains(Coord2(0, 1)) -> {
            when {
                coord.x > 0 && getConnectionOffsets(maze[coord + Coord2(-1, 0)]).contains(Coord2(1, 0)) -> '7'
                else -> throw IllegalArgumentException("Cannot replace start at $coord")
            }
        }
        else -> throw IllegalArgumentException("Cannot replace start at $§coord")
    }

fun countInsideTiles(loop: List<Pair<Coord2, Char>>, maze: Maze): Int {
    val loopCoords = loop.map { it.first }
    val loopCoordSet = loop.map { it.first }.toSet()
    val maxX = loopCoords.maxOfOrNull { it.x }!!
    val maxY = loopCoords.maxOfOrNull { it.y }!!
    var count = 0
    for (y in 0 .. maxY) {
        val stateMachine = CountTileStateMachine(loopCoordSet, maze)
        for (x in 0 .. maxX) {
            val coord= Coord2(x, y)
            stateMachine.handleTile(coord)
            if (! loopCoordSet.contains(coord) && stateMachine.isInside) count++
        }
    }
    return count
}

class CountTileStateMachine(private val loopCoords: Set<Coord2>, private val  maze: Maze, var isInside: Boolean = false) {
    private var onHorizontalLine = false
    private var startTile: Char? = null

    fun handleTile(coord: Coord2) {
        if (loopCoords.contains(coord)) {
            val currTile = if (maze[coord] == 'S') replaceStart(coord, maze) else maze[coord]
            when(currTile) {
                'L', 'F' -> if (! onHorizontalLine) {
                                onHorizontalLine = true
                                startTile = currTile
                            } else throw IllegalArgumentException("On horizontal line $currTile not expected coord=$coord")
                'J', '7' -> if (onHorizontalLine) {
                                onHorizontalLine = false
                                if (currTile == '7' && startTile == 'L' || currTile == 'J' && startTile == 'F')
                                    isInside = ! isInside
                                startTile = null
                            } else throw IllegalArgumentException("Outside a horizontal line $currTile not expected coord=$coord")
                '-' ->  if (!onHorizontalLine) throw IllegalArgumentException("Outside a horizontal line $currTile not expected coord=$coord")
                '.', '|' ->  if (onHorizontalLine) throw IllegalArgumentException("On a horizontal line $currTile not expected coord=$coord")
            }
            if (currTile == '|') isInside = ! isInside
        }
    }
}

fun Maze.get(coord: Coord2) = this[coord.x][coord.y]
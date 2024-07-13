import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

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
            Then("histories should be parsed correctly") {
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
                    loop.joinToString("") shouldBe "S-7|J-L|"
                }
            }
        }
        When("parsing the input 2") {
            val maze2 = parseMaze(exampleInput2Day10)
            Then("histories should be parsed correctly") {
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
                    loop.joinToString("") shouldBe "S-7|J-L|"
                }
            }
        }
        When("parsing the input 3") {
            val maze2 = parseMaze(exampleInput3Day10)
            Then("histories should be parsed correctly") {
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
                    loop.joinToString("") shouldBe "SJFJF7|L7J--FJL|"
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
        val start = findStart(maze)
        yield(maze[start.y][start.x] )
        val startConnections = getConnections('S', start, maze)
        var prevCoord = start
        var currCoord = startConnections[0]
        while(true) {
            val curr = maze[currCoord.y][currCoord.x]
            if (curr == 'S') break
            yield(curr)
            val nextConnections = getConnections(curr, currCoord, maze)
            val nextCoord = nextConnections.first { it != prevCoord }
            prevCoord = currCoord
            currCoord = nextCoord
        }
    }.toList()


fun getConnections(tile: Char, coord: Coord2, maze: Maze): List<Coord2> {
    val connectionOffsets = getConnectionOffsets(tile, coord, maze)
    return connectionOffsets.map { coord + it }.filter { maze[it.y][it.x] != '.' }
}

private fun getConnectionOffsets(tile: Char, coord: Coord2, maze: Maze): List<Coord2> = when(tile) {
    '|' -> listOf(Coord2(0, -1), Coord2(0, 1))
    '-' -> listOf(Coord2(-1, 0), Coord2(1, 0))
    'L' -> listOf(Coord2(0, -1), Coord2(1, 0))
    'J' -> listOf(Coord2(0, -1), Coord2(-1, 0))
    '7' -> listOf(Coord2(-1, 0), Coord2(0, 1))
    'F' -> listOf(Coord2(1, 0), Coord2(0, 1))
    '.' -> throw IllegalArgumentException("No connection for .")
    'S' -> listOf(
            Coord2(1, 0), Coord2(0, 1), Coord2(0, -1), Coord2(-1, 0)
        ).filter {
        (it + coord).x > 0 && (it + coord).y > 0
        }.filter { // for S check if it redirects back
            val next = it + coord
            val nextTile = maze[next.y][next.x]
            val backConnections = getConnectionOffsets(nextTile, next, maze).map { back -> back + next}
            backConnections.contains(coord)
        }
    else -> throw IllegalArgumentException("Unkown tile $tile")
}
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


val exampleInputDay15 = """
    .|...\....
    |.-.\.....
    .....|-...
    ........|.
    ..........
    .........\
    ..../.\\..
    .-.-/..|..
    .|....-|.\
    ..//.|....
""".trimIndent()
class Day16Part1: BehaviorSpec() { init {

    Given("mirror room input") {
        When("parsing the mirror rooum") {
            val mirrorRoom = parseMirrorRoom(exampleInputDay15)
            Then("it should have been parsed correclty") {
                mirrorRoom.size shouldBe 10
                mirrorRoom[0].size shouldBe 10
                mirrorRoom[0][1] shouldBe '|'
            }
            When("following beams") {
                val visited = MutableList(mirrorRoom.size) {
                    MutableList(mirrorRoom[0].size) { '.'}
                }
                visited[0][0] = '#'
                followBeams(listOf(Beam(Coord2(0, 0), Direction(1, 0))), mirrorRoom, visited)
                Then("it should have visited the right tiles") {
                    println(visited.map { it.joinToString("")}.joinToString("\n"))
                    visited shouldBe parseMirrorRoom("""
                        ######....
                        .#...#....
                        .#...#####
                        .#...##...
                        .#...##...
                        .#...##...
                        .#..####..
                        ########..
                        .#######..
                        .#...#.#..                        
                    """.trimIndent())
                }
                Then("it should count the right number of visited tiles") {
                    countVisited(visited) shouldBe 46
                }
            }
        }
    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay16.txt")!!
        val mirrorRoom = parseMirrorRoom(exerciseInput)
        When("following beams and counting visited tiles") {
            val visited = MutableList(mirrorRoom.size) {
                MutableList(mirrorRoom[0].size) { '.'}
            }
            visited[0][0] = '#'
            followBeams(listOf(Beam(Coord2(0, 0), Direction(1, 0))), mirrorRoom, visited)
            Then("it should have the right sum of visited tiles") {
                countVisited(visited) shouldBe 46
            }
        }
    }
} }

typealias MirrorRoom = List<List<Char>>
fun parseMirrorRoom(input: String) = input.split("\n").map {
    it.trim().toCharArray().toList()
}

fun followBeams(beams: List<Beam>, mirrorRoom: MirrorRoom, visited: MutableList<MutableList<Char>>) {
    fun insideRoom(pos: Coord2) =
        pos.y in 0 ..< mirrorRoom.size &&
            pos.x in 0 ..< mirrorRoom[pos.y].size

    fun moveBeam(beam: Beam): List<Beam> {
        val nextCoord2 = Coord2(beam.pos.x + beam.direction.deltaX, beam.pos.y + beam.direction.deltaY)
        val visited = beam.visited
        val result = if (insideRoom(nextCoord2)) {
            val tile = mirrorRoom[nextCoord2.y][nextCoord2.x]
            when {
                tile == '|' && beam.direction.deltaY == 0 -> listOf(
                    Beam(nextCoord2, Direction(0, -1), beam.visited.toMutableSet()),
                    Beam(nextCoord2, Direction(0, 1), beam.visited.toMutableSet())

                )
                tile == '-' && beam.direction.deltaX == 0 -> listOf(
                    Beam(nextCoord2, Direction(-1, 0), beam.visited.toMutableSet()),
                    Beam(nextCoord2, Direction(1, 0), beam.visited.toMutableSet())

                )
                tile == '/' -> listOf(
                    Beam(nextCoord2, Direction(-beam.direction.deltaY, -beam.direction.deltaX), beam.visited.toMutableSet())
                )
                tile == '\\' -> listOf(
                    Beam(nextCoord2, Direction(beam.direction.deltaY, beam.direction.deltaX), beam.visited.toMutableSet())
                )
                else -> listOf(Beam(nextCoord2, beam.direction, beam.visited))
            }
        }
        else listOf() // beam falls out of the room
        return result.filter { Pair(it.pos, it.direction) !in visited } // if visited the same tile with the same direction loop!
            .map { it.visited += Pair(it.pos, it.direction); it } // and add visited
    }
    var currentBeams = beams
    while(currentBeams.size > 0) {
        val nextBeams = mutableListOf<Beam>()
        for(beam in currentBeams) {
            val movedBeams = moveBeam(beam)
            for(movedBeam in movedBeams)
                    visited[movedBeam.pos.y][movedBeam.pos.x] = '#'
            nextBeams += movedBeams
        }
        currentBeams = nextBeams
    }
}

fun countVisited(mirrorRoom: MirrorRoom) =
    mirrorRoom.sumOf { row -> row.filter { it == '#' }.count() }

data class Beam(val pos: Coord2, val direction: Direction, val visited: MutableSet<Pair<Coord2, Direction>> = mutableSetOf())
data class Direction(val deltaX: Int, val deltaY: Int)

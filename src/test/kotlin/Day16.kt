import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


val exampleInputDay16 = """
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
        When("parsing the mirror room") {
            val mirrorRoom = parseMirrorRoom(exampleInputDay16)
            Then("it should have been parsed correctly") {
                mirrorRoom.size shouldBe 10
                mirrorRoom[0].size shouldBe 10
                mirrorRoom[0][1] shouldBe '|'
            }
            When("following beams") {
                val visited = MutableList(mirrorRoom.size) {
                    MutableList(mirrorRoom[0].size) { '.'}
                }
                followBeams(Beam(Coord2(0, 0), Direction(1, 0)), mirrorRoom, visited)
                Then("it should have visited the right tiles") {
                    println(visited.joinToString("\n") { it.joinToString("") })
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
            followBeams(Beam(Coord2(0, 0), Direction(1, 0)), mirrorRoom, visited)
            Then("it should have the right sum of visited tiles") {
                countVisited(visited) shouldBe 7046
            }
        }
    }
} }

class Day16Part2: BehaviorSpec() { init {

    Given("mirror room input") {
        val mirrorRoom = parseMirrorRoom(exampleInputDay16)
        When("following beams from edge 3, 0") {
            val count = followBeamsAndCount(Beam(Coord2(3, 0), Direction(0, 1)), mirrorRoom)
            Then("count should be right") {
                count shouldBe 51
            }
        }
        When("following beams from all edge tiles") {
            val counts = followBeamsAndCountFromAllEdes(mirrorRoom)
            Then("it should have followed 40 beams") {
                counts.size shouldBe 40
            }
            Then("counts should have the best") {
                counts.max() shouldBe 51
            }
        }

    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay16.txt")!!
        val mirrorRoom = parseMirrorRoom(exerciseInput)
        When("following beams from all edge tiles") {
            val counts = followBeamsAndCountFromAllEdes(mirrorRoom)
            Then("counts should have the best") {
                counts.max() shouldBe 7313
            }
        }
    }
} }

typealias MirrorRoom = List<List<Char>>
fun parseMirrorRoom(input: String) = input.split("\n").map {
    it.trim().toCharArray().toList()
}

fun followBeams(startBeam: Beam, mirrorRoom: MirrorRoom, visitedInRoom: MutableList<MutableList<Char>>) {
    fun insideRoom(pos: Coord2) =
        pos.y in mirrorRoom.indices &&
            pos.x in 0 ..< mirrorRoom[pos.y].size

    fun moveBeam(beam: Beam): List<Beam> {
        val nextCoord2 = Coord2(beam.pos.x + beam.direction.deltaX, beam.pos.y + beam.direction.deltaY)
        return if (insideRoom(nextCoord2)) {
            val tile = mirrorRoom[nextCoord2.y][nextCoord2.x]
            when {
                tile == '|' && beam.direction.deltaY == 0 -> listOf(
                    Beam(nextCoord2, Direction(0, -1)),
                    Beam(nextCoord2, Direction(0, 1))

                )
                tile == '-' && beam.direction.deltaX == 0 -> listOf(
                    Beam(nextCoord2, Direction(-1, 0)),
                    Beam(nextCoord2, Direction(1, 0))

                )
                tile == '/' -> listOf(
                    Beam(nextCoord2, Direction(-beam.direction.deltaY, -beam.direction.deltaX))
                )
                tile == '\\' -> listOf(
                    Beam(nextCoord2, Direction(beam.direction.deltaY, beam.direction.deltaX))
                )
                else -> listOf(Beam(nextCoord2, beam.direction))
            }
        }
        else listOf() // beam falls out of the room
    }

    visitedInRoom[startBeam.pos.y][startBeam.pos.x] = '#'
    var currentBeams = listOf(startBeam)
    val visitedTiles = mutableSetOf(startBeam)
    while(currentBeams.isNotEmpty()) {
        val nextBeams = mutableListOf<Beam>()
        for(beam in currentBeams) {
            val movedBeams = moveBeam(beam)
            for(movedBeam in movedBeams)
                    visitedInRoom[movedBeam.pos.y][movedBeam.pos.x] = '#'
            val filteredMovedBeams = movedBeams.filter { it !in visitedTiles} // ignore tiles visited with the same direction
            visitedTiles += filteredMovedBeams
            nextBeams += filteredMovedBeams
        }
        currentBeams = nextBeams
    }
}

fun followBeamsAndCount(beam: Beam, mirrorRoom: MirrorRoom): Int {
    val visited = MutableList(mirrorRoom.size) {
        MutableList(mirrorRoom[0].size) { '.'}
    }
    followBeams(beam, mirrorRoom, visited)
    //println(visited.joinToString("\n") { it.joinToString("") })
    return countVisited(visited)
}

fun followBeamsAndCountFromAllEdes(mirrorRoom: MirrorRoom) = buildList {
    // upper edges
    for (x in mirrorRoom[0].indices)
        add(followBeamsAndCount(Beam(Coord2(x, 0), Direction(0, 1)), mirrorRoom))
    // lower edges
    for (x in mirrorRoom[mirrorRoom.size-1].indices)
        add(followBeamsAndCount(Beam(Coord2(x, mirrorRoom.size-1), Direction(0, -1)), mirrorRoom))
    // left edges
    for (y in mirrorRoom.indices)
        add(followBeamsAndCount(Beam(Coord2(0, y), Direction(1, 0)), mirrorRoom))
    // right edges
    for (y in mirrorRoom.indices)
        add(followBeamsAndCount(Beam(Coord2(mirrorRoom[y].size - 1, y), Direction(-1, 0)), mirrorRoom))
}

fun countVisited(mirrorRoom: MirrorRoom) =
    mirrorRoom.sumOf { row -> row.count { it == '#' } }

data class Beam(val pos: Coord2, val direction: Direction)
data class Direction(val deltaX: Int, val deltaY: Int)

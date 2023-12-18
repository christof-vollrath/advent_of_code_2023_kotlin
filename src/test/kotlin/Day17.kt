import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.math.sign


val exampleInputDay17 = """
    2413432311323
    3215453535623
    3255245654254
    3446585845452
    4546657867536
    1438598798454
    4457876987766
    3637877979653
    4654967986887
    4564679986453
    1224686865563
    2546548887735
    4322674655533
""".trimIndent()

class Day17Part1: BehaviorSpec() { init {

    Given("city blocks input") {
        When("parsing input") {
            val cityBlock = parseCityBlocks(exampleInputDay17)
            Then("it should have been parsed correctly") {
                cityBlock.size shouldBe 13
                cityBlock[0].size shouldBe 13
                cityBlock[1][3] shouldBe 5
            }
            When("searching  possible moves from the upper left corner") {
                val moves = findMoves(Coord2(0, 0), cityBlock)
                Then("it should have found right moves") {
                    moves.size shouldBe 6
                    moves shouldBe
                            (List<CrucibleMove>(3) { i -> CrucibleMove(0, i+1) } +
                             List<CrucibleMove>(3) { i -> CrucibleMove(i+1, 0) }).toSet()
                }
            }
            When("searching possible moves from the middle") {
                val moves = findMoves(Coord2(5, 5), cityBlock, CrucibleMove(0, 1))
                Then("it should not go back") {
                    moves.size shouldBe 6
                    moves shouldContain CrucibleMove(deltaX=-3, deltaY=0)
                    moves shouldContain CrucibleMove(deltaX=3, deltaY=0)
                    moves shouldNotContain CrucibleMove(deltaX=0, deltaY=2)
                    moves shouldNotContain CrucibleMove(deltaX=0, deltaY=-1)
                }
            }
            When("searching possible moves at the border") {
                val moves = findMoves(Coord2(11, 2), cityBlock, CrucibleMove(1, 0))
                Then("it should not go back") {
                    moves.size shouldBe 5
                    moves shouldNotContain CrucibleMove(deltaX=1, deltaY=0)
                    moves shouldContain CrucibleMove(deltaX=0, deltaY=-1)
                    moves shouldContain CrucibleMove(deltaX=0, deltaY=3)
                    moves shouldNotContain CrucibleMove(deltaX=0, deltaY=-3)
                    moves shouldNotContain CrucibleMove(deltaX=2, deltaY=0)
                    moves shouldNotContain CrucibleMove(deltaX=-1, deltaY=0)
                }
            }
            When("moving") {
                val visited = setOf<Pair<Coord2, CrucibleMove>>()
                val (nextPos, heatLoss, nextVisited) = moveCrucible(Coord2(1, 1), CrucibleMove(2, 0), cityBlock, visited)
                Then("it schould calculate the right position, update the visited blocks and calculate the heat loss") {
                    nextPos shouldBe Coord2(3, 1)
                    heatLoss shouldBe 6
                    nextVisited shouldContainExactly setOf(
                        Pair(Coord2(x = 2, y = 1), CrucibleMove(deltaX = 2, deltaY = 0)),
                        Pair(Coord2(x = 3, y = 1), CrucibleMove(deltaX = 2, deltaY = 0))
                    )
                }
            }
            When("moving left") {
                val visited = setOf<Pair<Coord2, CrucibleMove>>()
                val (nextPos, heatLoss, nextVisited) = moveCrucible(Coord2(5, 5), CrucibleMove(-2, 0), cityBlock, visited)
                Then("it schould calculate the right position, update the visited blocks and calculate the heat loss") {
                    nextPos shouldBe Coord2(3, 5)
                    heatLoss shouldBe 13
                    nextVisited shouldContainExactly setOf(
                        Pair(Coord2(x = 4, y = 5), CrucibleMove(deltaX = -2, deltaY = 0)),
                        Pair(Coord2(x = 3, y = 5), CrucibleMove(deltaX = -2, deltaY = 0))
                    )
                }
            }
            When("searching minimal heat loss from destination -2") {
                val (heatLoss, path) = findMinimalHeatLoss(Coord2(11, 10), Coord2(cityBlock[0].size-1, cityBlock.size-1), cityBlock)
                Then("should have found the path with a minimal heat loss") {
                    heatLoss shouldBe 15
                    path.size shouldBe 2
                }
            }
            When("searching minimal heat loss from destination -3") {
                val (heatLoss, path) = findMinimalHeatLoss(Coord2(12, 7), Coord2(cityBlock[0].size-1, cityBlock.size-1), cityBlock)
                Then("should have found the path with a minimal heat loss") {
                    heatLoss shouldBe 31
                    path.size shouldBe 4
                }
            }
            When("searching minimal heat loss from destination -5") {
                val (heatLoss, path) = findMinimalHeatLoss(Coord2(11, 7), Coord2(cityBlock[0].size-1, cityBlock.size-1), cityBlock)
                Then("should have found the path with a minimal heat loss") {
                    heatLoss shouldBe 34
                    path.size shouldBe 5
                }
            }
            /*
            When("searching minimal heat loss") {
                val (heatLoss, path) = findMinimalHeatLoss(Coord2(0, 0), Coord2(cityBlock[0].size-1, cityBlock.size-1), cityBlock)
                Then("should have found the path with a minimal heat loss") {
                    heatLoss shouldBe 102
                    path.size shouldBe 0
                }
            }

             */
        }
    }
}}

typealias CityBlock = List<List<Int>>
fun parseCityBlocks(input: String) = input.split("\n").map {
    it.trim().toCharArray().map { c -> c.digitToInt() }
}

fun findMoves(pos: Coord2, cityBlocks: CityBlock, lastMove: CrucibleMove? = null): Set<CrucibleMove> = buildSet {
    fun backDirection(dx: Int, dy: Int, lastMove: CrucibleMove?) = if (lastMove != null) {
            if (dx == 0) dy.sign == -(lastMove.deltaY).sign
            else if (dy == 0) dx.sign == -(lastMove.deltaX).sign
            else throw IllegalArgumentException("Either dx or dy must be 0")
        } else false
    fun sameDirection(dx: Int, dy: Int, lastMove: CrucibleMove?) = if (lastMove != null) {
        if (dx == 0) dy.sign == lastMove.deltaY.sign
        else if (dy == 0) dx.sign == lastMove.deltaX.sign
        else throw IllegalArgumentException("Either dx or dy must be 0")
    } else false
    for (dx in -3..3)
        if (dx != 0 && pos.x + dx in cityBlocks[pos.y].indices)
            if (! backDirection(dx, 0, lastMove) && ! sameDirection(dx, 0, lastMove))
                add(CrucibleMove(dx, 0))
    for (dy in -3..3)
        if (dy != 0 && pos.y + dy in cityBlocks.indices)
            if (! backDirection(0, dy, lastMove) && ! sameDirection(0, dy, lastMove))
                add(CrucibleMove(0, dy))
}

fun moveCrucible(pos: Coord2, move: CrucibleMove, block: CityBlock, visited: Set<Pair<Coord2, CrucibleMove>>): Triple<Coord2, Int, Set<Pair<Coord2, CrucibleMove>>> {
    var heatLoss = 0
    var currPos = pos
    val currVisited = mutableSetOf<Pair<Coord2, CrucibleMove>>()
    for(dx in 1..abs(move.deltaX)) {
        currPos = Coord2(currPos.x + move.deltaX.sign, currPos.y) // move left or right
        currVisited += Pair(currPos, move)
        heatLoss += block[currPos.y][currPos.x]
    }
    for(dy in 1..abs(move.deltaY)) {
        currPos = Coord2(currPos.x, currPos.y + move.deltaY.sign) // move up or down
        currVisited += Pair(currPos, move)
        heatLoss += block[currPos.y][currPos.x]
    }
    return Triple(currPos, heatLoss, visited + currVisited)
}


private fun findMinimalHeatLoss(start: Coord2, destination: Coord2, cityBlock: List<List<Int>>): Pair<Int, List<CrucibleMove>> {
    var currentPaths = mapOf<Pair<Coord2, CrucibleMove?>, Triple<Int, List<CrucibleMove>, Set<Pair<Coord2, CrucibleMove>>>> (
        Pair(start, CrucibleMove(0, 1)) to Triple(cityBlock[start.y][start.x], emptyList(), setOf(Pair(start, CrucibleMove(0, 1)))),
        Pair(start, CrucibleMove(1, 0)) to Triple(cityBlock[start.y][start.x], emptyList(), setOf(Pair(start, CrucibleMove(1, 0))))
    )
    var solution: Pair<Int, List<CrucibleMove>>? = null
    while(currentPaths.isNotEmpty()) {
        println(currentPaths.size)
        val nextPaths: MutableMap<Pair<Coord2, CrucibleMove?>, Triple<Int, List<CrucibleMove>, Set<Pair<Coord2, CrucibleMove>>>> = mutableMapOf()
        for((posAndLastMove, heatLossAndPath) in currentPaths.entries) {
            val (pos, lastMove) = posAndLastMove
            val (heatLoss, path, visited) = heatLossAndPath
            val moves = findMoves(pos, cityBlock, lastMove)
            for (move in moves) {
                val (newPos, heatLossMove, newVisited) = moveCrucible(pos, move, cityBlock, visited)
//                if (Pair(newPos, move) !in visited &&
//                        (newPos !in currentSolutions  || currentSolutions[newPos]!!.first >= heatLoss + heatLossMove))
//                if (Pair(newPos, move) !in visited &&
                val nextHeadLoss = heatLoss + heatLossMove
                if (newPos == destination && (solution == null || solution.first > nextHeadLoss))
                    solution = Pair(heatLoss + heatLossMove, path + move)
                else {
                    if (solution == null || solution.first > nextHeadLoss) { // we don't need to consider path if we have a better solution
                        val newPosAndLastMove = Pair(newPos, move)
                        if (newPosAndLastMove !in visited  && (newPosAndLastMove !in currentPaths || currentPaths[newPosAndLastMove]!!.first > nextHeadLoss))
                            nextPaths[newPosAndLastMove] = Triple(nextHeadLoss, path + move, newVisited)
                    }
                }
            }
        }
        currentPaths = nextPaths
    }
    if (solution == null) throw IllegalArgumentException("no solution found")
    return Pair(solution.first, solution.second)
}

data class CrucibleMove(val deltaX: Int, val deltaY: Int)


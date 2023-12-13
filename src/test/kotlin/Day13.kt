import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay13 = """
        #.##..##.
        ..#.##.#.
        ##......#
        ##......#
        ..#.##.#.
        ..##..##.
        #.#.##.#.
        
        #...##..#
        #....#..#
        ..##..###
        #####.##.
        #####.##.
        ..##..###
        #....#..#
    """.trimIndent()

class Day13Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val mirrorMaps = parseMirrorMaps(exampleInputDay13)
            Then("map should be parsed mirror maps correctly") {
                mirrorMaps.size shouldBe 2
                mirrorMaps[0][0] shouldBe listOf('#', '.', '#', '#', '.', '.', '#', '#', '.')
            }
            Then("finding horizontal mirror for first map should not find one") {
                findHorizontalMirror(mirrorMaps[0]) shouldBe 0
            }
            Then("finding horizontal mirror for 2nd map should work") {
                findHorizontalMirror(mirrorMaps[1]) shouldBe 4
            }
            Then("finding vertical mirror for first map should should work") {
                findVerticalMirror(mirrorMaps[0]) shouldBe 5
            }
            Then("finding vertical mirror for 2nd map not find one") {
                findVerticalMirror(mirrorMaps[1]) shouldBe 0
            }
            When("finding mirrors and summing them") {
                val result = mirrorMaps.sumOf { findHorizontalMirror(it) * 100 + findVerticalMirror(it) }
                Then("it should find the solution") {
                    result shouldBe 405
                }
            }
        }
    }

    Given("a 2d list") {
        Then("pivot should turn is to the side") {
            pivot(listOf(
                listOf('#', '#'),
                listOf('.', '.')
            )) shouldBe listOf(
                listOf('#', '.'),
                listOf('#', '.')
            )
        }
    }

    Given("exercise input") {
        val mirrorMaps = parseMirrorMaps(readResource("inputDay13.txt")!!)
        When("finding mirrors and summing them") {
            val result = mirrorMaps.sumOf { findHorizontalMirror(it) * 100 + findVerticalMirror(it) }
            Then("it should find the solution") {
                result shouldBe 31739
            }
        }
    }

} }

fun parseMirrorMaps(input: String) = buildList {
    var parsedMap = mutableListOf<List<Char>>()
    for (line in input.split("\n").map { it.trim() }) {
        if (line.isEmpty()) {
            if (parsedMap.isNotEmpty()) add(parsedMap)
            parsedMap = mutableListOf()
        } else parsedMap += line.toCharArray().toList()
    }
    if (parsedMap.isNotEmpty()) add(parsedMap)
}

fun findHorizontalMirror(map: List<List<Char>>): Int {
    fun isMirror(pos: Int): Boolean {
        var i1 = pos - 1
        var i2 = pos
        while(i1 >= 0 && i2 < map.size) {
            if (map[i1] != map[i2]) return false
            i1--; i2++
        }
        return true
    }
    for(i in 1 until map.size)
        if (isMirror(i)) return i
    return 0
}

fun findVerticalMirror(map: List<List<Char>>) = findHorizontalMirror(pivot(map))

fun pivot(list2d: List<List<Char>>) = buildList {
    var resultLine = mutableListOf<Char>()
    for (x in list2d[0].indices) {
        for (y in list2d.indices)
            resultLine += list2d[y][x]
        add(resultLine)
        resultLine = mutableListOf()
    }
}
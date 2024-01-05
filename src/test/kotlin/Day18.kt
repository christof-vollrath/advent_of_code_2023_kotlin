import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.math.sign


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

class Day18Part2: BehaviorSpec() { init {

    Given("dig plan 1") {
        val digPlan = parseDigPlan(exampleInputDay18)
        When("finding highest and lowest values") {
            val minMax = findMinMax(digPlan)
            Then("it should have found the max y value"){
                minMax shouldBe Pair(LongCoord2(0, 0), LongCoord2(6L, 9L))
            }
        }
         When("finding corners") {
             val corners = findCorners(digPlan)
             corners shouldBe listOf(
                 LongCoord2(x = 0, y = 0), LongCoord2(x = 6, y = 0), LongCoord2(x = 6, y = 5),
                 LongCoord2(x = 4, y = 5), LongCoord2(x = 4, y = 7), LongCoord2(x = 6, y = 7), LongCoord2(x = 6, y = 9),
                 LongCoord2(x = 1, y = 9), LongCoord2(x = 1, y = 7), LongCoord2(x = 0, y = 7), LongCoord2(x = 0, y = 5),
                 LongCoord2(x = 2, y = 5), LongCoord2(x = 2, y = 2), LongCoord2(x = 0, y = 2)
             )
             When("finding highest and lowest values based on corners") {
                 val minMax = findMinMaxFromCorners(corners)
                 Then("it should have found the max y value"){
                     minMax shouldBe Pair(LongCoord2(0, 0), LongCoord2(6L, 9L))
                 }
                 When("finding upper left corner") {
                    val upperLeftCorner = findUpperLeftCorner(corners, minMax.first)
                    Then("it should have found upper left corner") {
                        upperLeftCorner shouldBe LongCoord2(0, 0)
                    }
                     When("resorting corners to start with upper left corner") {
                         val resorted = resortCorners(corners, minMax.first)
                         Then("it should start with the right corner") {
                             resorted.size shouldBe corners.size // should still contain all corners
                             resorted[0] shouldBe LongCoord2(0, 0)
                         }
                     }
                 }
             }
             When("calculating size directly") {
                 val size = calculateSize(digPlan)
                 Then("it should find the same size as in part 1") {
                     size shouldBe 62
                 }
             }
         }
     }
    Given("dig plan of first part exercise") {
        val exerciseInput = readResource("inputDay18.txt")!!
        val digPlan = parseDigPlan(exerciseInput)
        When("calculating size directly") {
            val size = calculateSize(digPlan)
            Then("it should find the same size as in part 1") {
                size shouldBe 53300
            }
        }
    }
    Given("dig plan 2") {
        val digPlan = parseDigPlan2(exampleInputDay18)
        When("finding highest and lowest values") {
            val corners = findCorners(digPlan)
            val minMax = findMinMaxFromCorners(corners)
            Then("it should have found the max y value") {
                minMax shouldBe Pair(LongCoord2(0, 0), LongCoord2(1186328L, 1186328L))
            }
            When("finding upper left corner") {
                val upperLeftCorner = findUpperLeftCorner(corners, minMax.first)
                Then("it should have found upper left corner") {
                    upperLeftCorner shouldBe LongCoord2(0, 0)
                }
            }
            When("resorting corners to start with upper left corner") {
                val resorted = resortCorners(corners, minMax.first)
                Then("it should start with the right corner") {
                    resorted.size shouldBe corners.size // should still contain all corners
                    resorted[0] shouldBe LongCoord2(0, 0)
                }
            }
            When("calculating size directly") {
                val size = calculateSize(digPlan)
                Then("it should find the right size") {
                    size shouldBe 952408144115L
                }
            }

        }
    }

    Given("exercise input") {
        val exerciseInput = readResource("inputDay18.txt")!!
        val digPlan = parseDigPlan2(exerciseInput)
        When("executing example input and drawing map") {
            val corners = findCorners(digPlan)
            val minMax = findMinMaxFromCorners(corners)
            Then("it should have found the min, max values") {
                minMax shouldBe Pair(LongCoord2(x=-3778733, y=-8868695), LongCoord2(x=9490626, y=2039161))
            }
            When("finding upper left corner") {
                val upperLeftCorner = findUpperLeftCorner(corners, minMax.first)
                Then("it should have found upper left corner") {
                    upperLeftCorner shouldBe LongCoord2(-3778733, -4090302)
                }
            }
            When("resorting corners to start with upper left corner") {
                val resorted = resortCorners(corners, minMax.first)
                Then("it should start with the right corner") {
                    resorted.size shouldBe corners.size // should still contain all corners
                    resorted[0] shouldBe LongCoord2(-3778733, -4090302)
                }
            }
            When("calculating size directly") {
                val size = calculateSize(digPlan)
                Then("it should find the right size") {
                    size shouldBe 64294334780659L
                }
            }
        }
    }

}}

fun findMinMax(digPlan: List<DigStep>): Pair<LongCoord2, LongCoord2> {
    var maxY = Long.MIN_VALUE
    var minY = Long.MAX_VALUE
    var posY = 0L
    var maxX = Long.MIN_VALUE
    var minX = Long.MAX_VALUE
    var posX = 0L
    for(step in digPlan) {
        when(step.dir) {
            DigDirection.UP -> posY -= step.nr
            DigDirection.DOWN ->  posY += step.nr
            DigDirection.RIGHT -> posX += step.nr
            DigDirection.LEFT -> posX -= step.nr
        }
        if (posY < minY) minY = posY
        if (posY > maxY) maxY = posY
        if (posX < minX) minX = posX
        if (posX > maxX) maxX = posX
    }
    return Pair(LongCoord2(minX, minY), LongCoord2(maxX, maxY))
}

fun findMinMaxFromCorners(corners: List<LongCoord2>): Pair<LongCoord2, LongCoord2> {
    var maxY = corners.maxBy { it.y }.y
    var minY = corners.minBy { it.y }.y
    var maxX = corners.maxBy { it.x }.x
    var minX = corners.minBy { it.x }.x
    return Pair(LongCoord2(minX, minY), LongCoord2(maxX, maxY))
}
/*
class Day18Part2: BehaviorSpec() { init {

    Given("dig plan 1") {
        val digPlan = parseDigPlan(exampleInputDay18)
        When("finding corners") {
            val corners = findCorners(digPlan)
            corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=6, y=0), LongCoord2(x=6, y=5),
                LongCoord2(x=4, y=5), LongCoord2(x=4, y=7), LongCoord2(x=6, y=7), LongCoord2(x=6, y=9),
                LongCoord2(x=1, y=9), LongCoord2(x=1, y=7), LongCoord2(x=0, y=7), LongCoord2(x=0, y=5),
                LongCoord2(x=2, y=5), LongCoord2(x=2, y=2), LongCoord2(x=0, y=2))
            println(corners)
        }
        When("finding rectangles") {
            val rectangles = findRectangles(findCorners(digPlan))
            Then("rectangles should be right") {
                rectangles shouldBe listOf(LongRectangle(c1=LongCoord2(x=2, y=4), c2=LongCoord2(x=5, y=0)),
                    LongRectangle(c1=LongCoord2(x=0, y=2), c2=LongCoord2(x=2, y=0)))
            }
        }
    }

    Given("dig plan 2") {
        When("parsing input") {
            val digPlan = parseDigPlan2(exampleInputDay18)
            Then("it should have been parsed correctly") {
                digPlan.size shouldBe 14
                digPlan[0] shouldBe DigStep(DigDirection.RIGHT, 461937)
                digPlan[2] shouldBe DigStep(DigDirection.RIGHT, 356671)
            }
            When("filling the interior") {
                val nr = calculatedFilledAfterPlan(digPlan)
                Then("it should have filled all interior cubic meters") {
                    nr shouldBe 952408144115L
                }
            }
        }
    }
    Given("a real simple dig plan") {
        val digPlan = parseDigPlan("""
            R 3 (#70c710)
            D 2 (#0dc571)
            L 3 (#8ceee2)
            U 2 (#caa173)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            Then("map should be right") {
                mapStr shouldBe """
                    ####
                    #..#
                    ####""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=3, y=0), LongCoord2(x=3, y=2), LongCoord2(x=0, y=2))
                println(corners)
            }
            When("finding rectangles") {
                val rectangles = findRectangles(findCorners(digPlan))
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(LongRectangle(LongCoord2(0, 2), LongCoord2(3, 0)))
                }
            }
        }
    }

    Given("a simple dig plan with two rectangles") {
        val digPlan = parseDigPlan("""
            R 5 (#70c710)
            D 4 (#0dc571)
            L 3 (#8ceee2)
            U 2 (#caa173)
            L 2 (#8ceee2)
            U 2 (#caa173)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            Then("map should be right") {
                mapStr shouldBe """
                    ######
                    #....#
                    ###..#
                    ..#..#
                    ..####""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=5, y=0),
                    LongCoord2(x=5, y=4), LongCoord2(x=2, y=4), LongCoord2(x=2, y=2), LongCoord2(x=0, y=2))
                println(corners)
            }
            When("finding rectangles") {
                val rectangles = findRectangles(findCorners(digPlan))
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(LongRectangle(c1=LongCoord2(x=2, y=4), c2=LongCoord2(x=5, y=0)),
                        LongRectangle(c1=LongCoord2(x=0, y=2), c2=LongCoord2(x=2, y=0)))
                }
            }
        }
    }

    Given("a simple dig plan with two rectangles in other variant") {
        val digPlan = parseDigPlan("""
            R 5 (#70c710)
            D 2 (#0dc571)
            L 3 (#8ceee2)
            D 2 (#caa173)
            L 2 (#8ceee2)
            U 4 (#caa173)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            Then("map should be right") {
                mapStr shouldBe """
                    ######
                    #....#
                    #.####
                    #.#...
                    ###...""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=5, y=0),
                    LongCoord2(x=5, y=2), LongCoord2(x=2, y=2), LongCoord2(x=2, y=4), LongCoord2(x=0, y=4))
                println(corners)
            }
            When("finding rectangles") {
                val rectangles = findRectangles(findCorners(digPlan))
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(LongRectangle(c1=LongCoord2(x=2, y=2), c2=LongCoord2(x=5, y=0)),
                        LongRectangle(c1=LongCoord2(x=0, y=4), c2=LongCoord2(x=2, y=0)))
                }
            }
        }
    }

    Given("a simple dig plan with two rectangles in yet other variant") {
        val digPlan = parseDigPlan("""
            R 3 (#70c710)
            D 2 (#0dc571)
            R 2 (#8ceee2)
            D 2 (#caa173)
            L 5 (#8ceee2)
            U 4 (#caa173)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            Then("map should be right") {
                mapStr shouldBe """
                    ####..
                    #..#..
                    #..###
                    #....#
                    ######""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=3, y=0), LongCoord2(x=3, y=2),
                    LongCoord2(x=5, y=2), LongCoord2(x=5, y=4), LongCoord2(x=0, y=4))
                println(corners)
            }
            When("finding rectangles") {
                val rectangles = findRectangles(findCorners(digPlan))
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(LongRectangle(c1=LongCoord2(x=0, y=4), c2=LongCoord2(x=5, y=2)),
                        LongRectangle(c1=LongCoord2(x=0, y=2), c2=LongCoord2(x=3, y=0)))
                }
            }
        }
    }

    Given("another variant of a dig plan another variant") {
        val digPlan = parseDigPlan("""
            R 5 (#70c710)
            D 2 (#0dc571)
            L 7 (#8ceee2)
            U 2 (#caa173)
            R 2 (#caa173)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            Then("map should be right") {
                mapStr shouldBe """
                    ########
                    #......#
                    ########""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                corners shouldBe listOf(LongCoord2(x=0, y=0), LongCoord2(x=5, y=0),
                    LongCoord2(x=5, y=2), LongCoord2(x=-2, y=2), LongCoord2(x=-2, y=0))
                println(corners)
            }
            When("finding rectangles") {
                val rectangles = findRectangles(findCorners(digPlan))
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(LongRectangle(c1=LongCoord2(x=2, y=2), c2=LongCoord2(x=5, y=0)),
                        LongRectangle(c1=LongCoord2(x=0, y=4), c2=LongCoord2(x=2, y=0)))
                }
            }
        }
    }

    /* TODO
    Given("another dig plan") {
        val digPlan = parseDigPlan("""
            R 3 (#70c710)
            D 2 (#0dc571)
            R 2 (#5713f0)
            D 3 (#d2c081)
            R 1 (#59c680)
            D 2 (#411b91)
            L 3 (#8ceee2)
            U 3 (#caa173)
            L 2 (#1b58a2)
            U 1 (#caa171)
            L 1 (#7807d2)
            U 3 (#a77fa3)
        """.trimIndent()) // Using parser of part 1 for better readability
        When("executing simple plan and drawing map") {
            val map = executePlan(digPlan)
            val mapStr = drawDigMap(map)
            println(drawDigMap(map)); repeat(2) { println() }
            Then("map should be right") {
                mapStr shouldBe """
                    ####...
                    #..#...
                    #..###.
                    ##...#.
                    .###.#.
                    ...#.##
                    ...#..#
                    ...####""".trimIndent()
            }
            When("finding corners") {
                val corners = findCorners(digPlan)
                println(corners)
            }
            When("finding regtancles") {
                val rectangles = findRectangles(digPlan)
                Then("rectangles should be right") {
                    rectangles shouldBe listOf(
                        LongRectangle(LongCoord2(0, 0), LongCoord2(0, 3)),
                        LongRectangle(LongCoord2(1, 0), LongCoord2(2, 4)),
                        LongRectangle(LongCoord2(3, 0), LongCoord2(3, 7)),
                        LongRectangle(LongCoord2(4, 2), LongCoord2(5, 7)),
                        LongRectangle(LongCoord2(6, 5), LongCoord2(6, 7)),
                    )
                }
            }
        }
    }

     */
    Given("exercise input") {
        val exerciseInput = readResource("inputDay18.txt")!!
        val digPlan = parseDigPlan2(exerciseInput)
        When("finding corners") {
            val corners = findCorners(digPlan)
            println(corners)
        }
        When("executing example input and drawing map") {
            val nr = calculatedFilledAfterPlan(digPlan)
            Then("it should have filled all interior cubic meters") {
                nr shouldBe 952408144115L
            }
        }
    }
} }


 */
class FindRectanglesTests : FunSpec({
    context("calculate size") {
        withData(
            // only one rectangle
            """
                R 3 (#70c710)
                D 2 (#0dc571)
                L 3 (#8ceee2)
                U 2 (#caa173)
            """.trimIndent() to 12,
            """
                L 4 (#70c710)
                D 2 (#0dc571)
                R 4 (#8ceee2)
                U 2 (#caa173)
            """.trimIndent() to 15,
            """
                R 3 (#70c710)
                U 2 (#0dc571)
                L 3 (#8ceee2)
                D 2 (#caa173)
            """.trimIndent() to 12,
            """
                L 3 (#70c710)
                U 2 (#0dc571)
                R 3 (#8ceee2)
                D 2 (#caa173)
            """.trimIndent() to 12,
            // Two rectangles
            """
                R 5 (#70c710)
                D 4 (#0dc571)
                L 3 (#8ceee2)
                U 2 (#caa173)
                L 2 (#8ceee2)
                U 2 (#caa173)
            """.trimIndent() to 26,
            """
                L 5 (#70c710)
                D 4 (#0dc571)
                R 3 (#8ceee2)
                U 2 (#caa173)
                R 2 (#8ceee2)
                U 2 (#caa173)
            """.trimIndent() to 26,
            """
                R 5 (#70c710)
                U 4 (#0dc571)
                L 3 (#8ceee2)
                D 2 (#caa173)
                L 2 (#8ceee2)
                D 2 (#caa173)
            """.trimIndent() to 26,
            """
                L 5 (#70c710)
                U 4 (#0dc571)
                R 3 (#8ceee2)
                D 2 (#caa173)
                R 2 (#8ceee2)
                D 2 (#caa173)
            """.trimIndent() to 26,
        ) { (plan, expected) ->
            // Using algorithm from part 1
            val digPlan = parseDigPlan(plan)
            val map = executePlan(digPlan)
            val filledMap = fillInterior(map)
            filledMap.size shouldBe expected
            // Optimized algorithm for part 2
            val size = calculateSize(digPlan)
            size shouldBe expected
        }
    }
})
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
    val nr = nrStr.toLong()
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

fun parseDigPlan2(input: String) = input.split("\n").map {
    parseDigStep2(it.trim())
}

fun parseDigStep2(line: String): DigStep {
    val regex = """([RLUD]) (\d+) \(#([0-9a-f]+)\)""".toRegex()
    val (_, _, hexStr) = regex
        .matchEntire(line)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect input line $line")
    val dir = when(hexStr[5]) {
        '0' -> DigDirection.RIGHT
        '2' -> DigDirection.LEFT
        '3' -> DigDirection.UP
        '1' -> DigDirection.DOWN
        else -> throw IllegalArgumentException("Unknown direction ${hexStr[5]}")
    }
    val nr = hexStr.substring(0, 5).toLong(16)
    return DigStep(dir, nr)
}


fun findCorners(digPlan: List<DigStep>): List<LongCoord2> = buildList {
    val startCorner = LongCoord2(0, 0)
    var pos = startCorner
    add(pos)
    var recentDirection: DigDirection? = null
    var startReached = false
    for(step in digPlan) {
        if (startReached) throw IllegalArgumentException("Plan continues after reaching start")
        when(step.dir) {
            DigDirection.RIGHT -> {
                if (recentDirection == DigDirection.RIGHT || recentDirection == DigDirection.LEFT) throw IllegalArgumentException("Right after horicontal movement is illegal")
                pos = LongCoord2(pos.x + step.nr, pos.y)
            }
            DigDirection.LEFT -> {
                if (recentDirection == DigDirection.RIGHT || recentDirection == DigDirection.LEFT) throw IllegalArgumentException("Left after horicontal movement is illegal")
                pos = LongCoord2(pos.x - step.nr, pos.y)
            }
            DigDirection.UP -> {
                if (recentDirection == DigDirection.UP || recentDirection == DigDirection.DOWN) throw IllegalArgumentException("Up after vertical movement is illegal")
                pos = LongCoord2(pos.x, pos.y - step.nr)
            }
            DigDirection.DOWN -> {
                if (recentDirection == DigDirection.UP || recentDirection == DigDirection.DOWN) throw IllegalArgumentException("Down after vertical movement is illegal")
                pos = LongCoord2(pos.x, pos.y + step.nr)
            }
        }
        recentDirection = step.dir
        if (pos == startCorner) startReached = true
        else add(pos)
    }
}

fun findRectangles(corners: List<LongCoord2>) = buildList {
    val cornerStack = ArrayDeque<LongCoord2>()
    for (corner in corners) cornerStack.addFirst(corner)
    while(cornerStack.size >= 4) {
        for (i in cornerStack.indices) { // Try to find one rectancle
            var rectangle: LongRectangle? = null
            val corner = cornerStack.get(cornerStack.size - 1 - i) // corners counted backward
            val corner_1 = cornerStack.get(cornerStack.size - 2 - i)
            val corner_2 = cornerStack.get(cornerStack.size - 3 - i)
            val corner_3 = cornerStack.get(cornerStack.size - 4 - i)
            val oppositeSide = corner_2 - corner_3
            if (oppositeSide.sign().x == 0) { // parallel to y axis
                val side = corner_1 - corner
                if (side.x != 0L) throw IllegalArgumentException("no opposite sides")
                if (oppositeSide.sign().y == side.sign().y)
                    rectangle = LongRectangle(corner, corner_2)
            } else if (oppositeSide.sign().y == 0) { // parallel to x axis
                val side = corner_1 - corner
                if (side.y != 0L) throw IllegalArgumentException("no opposite sides")
                if (oppositeSide.sign().x == side.sign().x)
                    rectangle = LongRectangle(corner, corner_2)
            } else throw IllegalArgumentException("sides should be parallel to x or y axis")
            if (rectangle != null) {
                add(rectangle)
                repeat(3) { cornerStack.removeLast() }
                cornerStack.add(LongCoord2(rectangle.c1.x, rectangle.c2.y))
                break // One found
            }
        }
        throw IllegalArgumentException("No rectangle found")
    }
}
/*
fun findRectangles(digPlan: List<DigStep>) = buildList {
    var pos = LongCoord2(0, 0)
    val movingRight = mutableListOf<LongCoord2>()
    var movingLeft = mutableListOf<LongCoord2>()
    for(step in digPlan) {
        when(step.dir) {
            DigDirection.RIGHT -> {
                pos = LongCoord2(pos.x + step.nr, pos.y)
                movingRight += pos
            }
            DigDirection.LEFT -> {
                pos = LongCoord2(pos.x - step.nr, pos.y)
                movingLeft += pos
            }
            DigDirection.UP -> {
                pos = LongCoord2(pos.x, pos.y - step.nr)
            }
            DigDirection.DOWN -> {
                pos = LongCoord2(pos.x, pos.y + step.nr)
            }
        }
    }
    movingLeft.reverse()
    var rightI = 0; var leftI = 0; var currPos = LongCoord2(0, 0)
    while(rightI < movingRight.size && leftI < movingLeft.size) {
        val rightX = movingRight[rightI].x
        val leftX = movingLeft[leftI].x
        if (rightX < leftX) {
            add(LongRectangle(currPos, LongCoord2(rightX, movingLeft[leftI].y)))
            rightI++
        }
        else {
            add(LongRectangle(currPos, LongCoord2(leftX, movingLeft[leftI].y)))
            leftI++
        }
    }
}

 */

fun calculatedFilledAfterPlan(digPlan: List<DigStep>): Long {
    return 0
}

fun findUpperLeftCorner(corners: List<LongCoord2>, minCoord: LongCoord2) =
    corners.filter { it.x == minCoord.x }.minBy { it.y }

fun resortCorners(corners: List<LongCoord2>, min: LongCoord2): List<LongCoord2> {
    val upperLeftCorner = findUpperLeftCorner(corners, min)
    val pos = corners.indexOf(upperLeftCorner)
    val result = corners.subList(pos, corners.size) + corners.subList(0, pos)
    val firstEdge = result[1] - result[0]
    if (firstEdge.y != 0L || firstEdge.x <= 0L) throw IllegalArgumentException("Should go clockwise instead $firstEdge")
    return result

}


fun calculateSize(digPlan: List<DigStep>): Long {
    val corners = findCorners(digPlan)
    val (min, max) = findMinMaxFromCorners(corners)
    val resorted = resortCorners(corners, min)
    var recentCorner = resorted.first()
    var size = 0L
    var recentVerticalDirectionUp = true // moving clockwise
    for(i in 1 until resorted.size) {
        val corner = resorted[i]
        val edge = corner - recentCorner
        if (edge.y == 0L) {
            val goingToTheRight = edge.x > 0
            val innerWidth = abs(edge.x) - 1
            val nextVerticalDirectionUp = i == resorted.size - 1 // after last corner it goes up
                    || with(resorted[i+1]) {
                val nextEdge = this - corner
                nextEdge.y < 0
            }
            var width = innerWidth
            if (goingToTheRight) {
                if (recentVerticalDirectionUp) width++
                if (!nextVerticalDirectionUp) width++
            } else {
                if (!recentVerticalDirectionUp) width++
                if (nextVerticalDirectionUp) width++
            }
            val height = if (goingToTheRight) max.y - recentCorner.y + 1 else max.y - recentCorner.y
            val rectangleSize = width * height
            if (goingToTheRight)  // add or remove rectangles ranging from the edge to the bottom
                size += rectangleSize
            else
                size -= rectangleSize
            recentVerticalDirectionUp = nextVerticalDirectionUp
        }
        recentCorner = corner
    }
    return size
}


enum class DigDirection { RIGHT, LEFT, UP, DOWN }

data class DigStep(val dir: DigDirection, val nr: Long)

data class LongCoord2(val x: Long, val y: Long) {
    operator fun minus(direction: LongCoord2) = LongCoord2(x - direction.x, y - direction.y)

    fun sign() = Coord2(x.sign, y.sign)

}
data class LongRectangle(val c1: LongCoord2, val c2: LongCoord2)


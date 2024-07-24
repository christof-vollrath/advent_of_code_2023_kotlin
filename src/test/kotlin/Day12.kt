import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlin.math.max


val exampleInputDay12 = """
        ???.### 1,1,3
        .??..??...?##. 1,1,3
        ?#?#?#?#?#?#?#? 1,3,1,6
        ????.#...#... 4,1,1
        ????.######..#####. 1,6,5
        ?###???????? 3,2,1
    """.trimIndent()

class Day12Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val springStatesAndGroups = parseSpringStatesAndGroupsLine(exampleInputDay12)
            Then("map should be parsed correctly") {
                springStatesAndGroups.size shouldBe 6
                springStatesAndGroups[0].first shouldBe listOf(SpringState.UNKNOWN, SpringState.UNKNOWN, SpringState.UNKNOWN, SpringState.OPERATIONAL, SpringState.DAMAGED, SpringState.DAMAGED, SpringState.DAMAGED)
                springStatesAndGroups[0].second shouldBe listOf(1, 1, 3)
            }
            When("Searching possible states") {
                val counts = springStatesAndGroups.map { (states, groups) -> searchPossibleStates(states, groups).count() }
                Then("it should have found the right counts") {
                    counts shouldBe listOf(1, 4, 1, 1, 4, 10)
                }
                Then("Summing counts should return right value") {
                    counts.sum() shouldBe 21
                }
            }
        }
    }

    Given("list of spring states") {
        val springStates = parseSpringStates("#.#.###")
        Then("Checking for groups should return right groups") {
            findDamagedGroups(springStates) shouldBe listOf(1, 1, 3)
        }
    }
    Given("list of spring states partially") {
        val springStates = parseSpringStates("#.#.?##")
        Then("Checking for groups partially should return right groups") {
            findDamagedGroupsPartially(springStates) shouldBe listOf(1, 1)
        }
    }

    Given("spring states with no unknown states") {
        val springStates = parseSpringStates("#.#.###")
        Then ("searching possible states should return this list if it matches groups") {
            searchPossibleStates(springStates, listOf(1, 1, 3)) shouldBe listOf(springStates)
        }
        Then ("searching possible states should return empty if it doesn't matches groups") {
            searchPossibleStates(springStates, listOf(2, 1, 3)) shouldBe listOf()
        }
    }

    Given("spring states with one unknown state at the beginning") {
        val springStates = parseSpringStates("?.#.###")
        Then ("searching possible states should return right states") {
            searchPossibleStates(springStates, listOf(1, 1, 3)) shouldBe listOf(parseSpringStates("#.#.###"))
        }
    }
    Given("spring states with one unknown state at the end") {
        val springStates = parseSpringStates("#.#.##?")
        Then ("searching possible states should return right states") {
            searchPossibleStates(springStates, listOf(1, 1, 3)) shouldBe listOf(parseSpringStates("#.#.###"))
        }
    }
    Given("spring states with three unknown states and one solution") {
        val springStates = parseSpringStates("???.###")
        Then ("searching possible states should return right states") {
            searchPossibleStates(springStates, listOf(1, 1, 3)) shouldBe listOf(parseSpringStates("#.#.###"))
        }
    }
    Given("spring states with five unknown states and four solution") {
        val springStates = parseSpringStates(".??..??...?##.")
        Then ("searching possible states should return right solutions") {
            searchPossibleStates(springStates, listOf(1, 1, 3)) shouldContainExactly  listOf(
                parseSpringStates("..#...#...###."),
                parseSpringStates("..#..#....###."),
                parseSpringStates(".#....#...###."),
                parseSpringStates(".#...#....###.")
            )
        }
    }
    Given("find all groups of operational springs") {
        Given("a simple group list") {
            val groups = listOf(1, 1, 3)
            When("searching for intervals") {
                val intervals = searchOperationalGroups(groups, 7)
                Then("it should find the right operational groups") {
                    intervals shouldBe listOf(listOf(0, 1, 1))
                }
            }
            When("searching for intervals with more choices") {
                val intervals1 = searchOperationalGroups(groups, 8)
                Then("it should find the right intervals") {
                    intervals1 shouldBe listOf(listOf(0, 1, 1), listOf(0, 1, 2), listOf(0, 2, 1), listOf(1, 1, 1))
                }
            }
            When("searching for intervals with even more choices") {
                val intervals1 = searchOperationalGroups(groups, 9)
                Then("it should find the right intervals") {
                    intervals1 shouldBe listOf(listOf(0, 1, 1), listOf(0, 1, 2), listOf(0, 1, 3),
                        listOf(0, 2, 1), listOf(0, 2, 2), listOf(0, 3, 1),
                        listOf(1, 1, 1), listOf(1, 1, 2), listOf(1, 2, 1), listOf(2, 1, 1)
                    )
                }
            }
        }
    }
    Given("condition records and operational/damaged groups") {
        Given("a operational and a damaged group") {
            val damagedGroups = listOf(1, 1, 3)
            val operationalGroups = listOf(0, 1, 1)
            When("checking if groups match to records") {
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#.###")) shouldBe true
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#.###.")) shouldBe true // ignore operational at the end
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#??##")) shouldBe true
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#??##?")) shouldBe true // ignore operational at the end
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates(".#.#.###")) shouldBe false
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("###.###")) shouldBe false
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#..###")) shouldBe false
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#.##")) shouldBe false
                matchGroupsAndRecords(damagedGroups, operationalGroups, parseSpringStates("#.#.####")) shouldBe false
            }
        }
    }
    Given("searching possible states starting with groups") {
        val springStatesAndGroups = parseSpringStatesAndGroupsLine(exampleInputDay12)
        springStatesAndGroups.size shouldBe 6
        When("Searching possible states from groups") {
            val counts = springStatesAndGroups.map { (states, groups) -> searchPossibleOperationalGroups(states, groups).count() }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1, 4, 1, 1, 4, 10)
            }
            Then("Summing counts should return right value") {
                counts.sum() shouldBe 21
            }
        }
    }

    Given("exercise input") {
        val springStatesAndGroups = parseSpringStatesAndGroupsLine(readResource("inputDay12.txt")!!)
        When("Searching possible states") {
            val counts = springStatesAndGroups.map { (states, groups) -> searchPossibleStates(states, groups).count() }
            Then("Summing counts should return right value") {
                counts.sum() shouldBe 8270
            }
        }
    }
} }

class Day12Part2: BehaviorSpec() { init {

    Given("an example") {
        val (states, groups) = parseSpringStatesAndGroups(".??..??...?##. 1,1,3")
        Then("counting possible states for folded input should be right") {
            countPossibleStatesFoldedM1(states, groups, 4) shouldBe 16384L
        }
    }

    Given("another example") {
        val (states, groups) = parseSpringStatesAndGroups("???.### 1,1,3")
        Then("counting possible states for folded input should be right") {
            countPossibleStatesFoldedM2(states, groups, 2) shouldBe 1
        }
    }

    Given("example input") {
        val springStatesAndGroups = parseSpringStatesAndGroupsLine(exampleInputDay12)
        When("Searching possible states without folding") {
            val counts = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM2(states, groups, 0) }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1L, 4L, 1L, 1L, 4L, 10L)
            }
        }
        When("Searching possible states with folding 2") {
            val counts = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM2(states, groups, 1) }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1L, 32L, 1L, 2L, 20L, 150L)
            }
        }
        When("Searching possible states with folding 3") {
            val counts = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM2(states, groups, 2) }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1L, 256L, 1L, 4L, 100L, 2250L)
            }
        }
        When("Searching possible states with folding 5") {
            val counts = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM1(states, groups, 4) }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1L, 16384L, 1L, 16L, 2500L, 506250L)
            }
            Then("Summing counts should return right value") {
                counts.sum() shouldBe 525152L
            }
        }
        When("Searching possible states with folding 5, alternative implementation") {
            val counts = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM2(states, groups, 4) }
            Then("it should have found the right counts") {
                counts shouldBe listOf(1L, 16384L, 1L, 16L, 2500L, 506250L)
            }
            Then("Summing counts should return right value") {
                counts.sum() shouldBe 525152L
            }
        }
    }
    Given("exercise input") {
        val input = readResource("inputDay12.txt")!!
        val springStatesAndGroups = parseSpringStatesAndGroupsLine(input)
        xWhen("Searching possible states") {
            val countsM1 = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM1(states, groups, 4) }
            val countsM2 = springStatesAndGroups.map { (states, groups) -> countPossibleStatesFoldedM2(states, groups, 4) }
            Then("find the difference between both methods") {
                val countsZipped = countsM1.zip(countsM2)
                val lines = input.split("\n")
                for ( (i, countPair) in countsZipped.withIndex()) {
                    if (countPair.first != countPair.second) println("difference in counts at $i: ${countPair.first} ${countPair.second} ${lines[i]}")
                }
            }
            Then("Summing counts should return right value") {
                val sumM1 = countsM1.sum()
                val sumM2 = countsM2.sum()
                println("sums $sumM1 $sumM2")
                sumM1 shouldBeGreaterThan  100659232962052L
                println(sumM1)
            }
        }
    }

} }

fun parseSpringStatesAndGroupsLine(input: String) = input.split("\n").map { line ->
    val parts = line.trim().split(" ")
    val states = parseSpringStates(parts[0])

    val groups = parts[1].split(",").map { it.toInt() }
    states to groups
}

fun parseSpringStatesAndGroups(line: String): Pair<List<SpringState>, List<Int>> {
    val parts = line.trim().split(" ")
    val states = parseSpringStates(parts[0])

    val groups = parts[1].split(",").map { it.toInt() }
    return states to groups
}

fun parseSpringStates(input: String) = input.toCharArray().map {
    when(it) {
        '?' -> SpringState.UNKNOWN
        '.' -> SpringState.OPERATIONAL
        '#' -> SpringState.DAMAGED
        else -> throw IllegalArgumentException("Unexpected spring condition $it")
    }
}

fun findDamagedGroups(states: List<SpringState>) = buildList {
    var currentGroupLength = 0
    for (state in states) {
        if (state == SpringState.DAMAGED) {
            currentGroupLength++
        } else {
            if (currentGroupLength > 0) { // end of current group
                add(currentGroupLength)
                currentGroupLength = 0
            }
        }
    }
    if (currentGroupLength > 0) // handle last group
        add(currentGroupLength)
}

fun findDamagedGroupsPartially(states: List<SpringState>): List<Int> {
    val result = mutableListOf<Int>()
    var currentGroupLength = 0
    for (state in states) {
        if (state == SpringState.DAMAGED) {
            currentGroupLength++
        } else {
            if (state == SpringState.UNKNOWN) return result // return only up to first unknown
            if (currentGroupLength > 0) { // end of current group
                result.add(currentGroupLength)
                currentGroupLength = 0
            }
        }
    }
    if (currentGroupLength > 0) // handle last group
        result.add(currentGroupLength)
    return result
}

fun compareDamagedGroupsPartially(states: List<SpringState>, groups: List<Int>): Boolean {
    val partialGroups = findDamagedGroupsPartially(states)
    return groups.take(partialGroups.size) == partialGroups
}

fun searchPossibleStates(states: List<SpringState>, groups: List<Int>): List<List<SpringState>> {
    fun replaceFirstUnknown(states: List<SpringState>): List<List<SpringState>> {
        val firstUnknown = states.indexOfFirst { it == SpringState.UNKNOWN }
        return if (firstUnknown < 0) listOf(states) // no unknown
        else {
            val v1 = states.subList(0, firstUnknown) + listOf(SpringState.OPERATIONAL) + states.drop(firstUnknown + 1)
            val v2 = states.subList(0, firstUnknown) + listOf(SpringState.DAMAGED) + states.drop(firstUnknown + 1)
            listOf(v1, v2)
        }
    }
    val replacedUnknown = replaceFirstUnknown(states)
    return if (replacedUnknown.size == 1) // Nothing unknown
        if (findDamagedGroups(replacedUnknown[0]) == groups) listOf(replacedUnknown[0])
        else listOf()
    else {
        val filteredReplacedUnknown = replacedUnknown.filter { compareDamagedGroupsPartially(it, groups) } // Keep only groups with match
        filteredReplacedUnknown.flatMap { searchPossibleStates(it, groups) }
    }
}

fun searchOperationalGroups(damagedGroups: List<Int>, length: Int, first: Boolean = true): List<List<Int>> {
    val sumLengthDamaged = damagedGroups.sum()
    val minimalSeparatorLength = damagedGroups.size - 1
    val range = (if (first) 0 else 1 )..length-sumLengthDamaged-minimalSeparatorLength
    return sequence {
        val nextDamagedGroups = damagedGroups.drop(1)
        val currDamagedGroup = damagedGroups.first()
        for (i in range) {
            val currList = listOf(i)
            if (nextDamagedGroups.isEmpty()) yield(currList)
            else {
                val groups = searchOperationalGroups(damagedGroups.drop(1), length - currDamagedGroup- i, false)
                groups.forEach {
                    yield(listOf(i) + it)
                }
            }
        }
    }.toList()
}

fun matchGroupsAndRecords(damagedGroups: List<Int>, operationalGroups: List<Int>, records: List<SpringState>): Boolean {
    var recordsI = 0
    for(i in damagedGroups.indices) {
        val damagedGroup = damagedGroups[i]
        val operationalGroup = operationalGroups[i]
        for (ogi in 0 until operationalGroup) {
            if (recordsI < records.size && records[recordsI] != SpringState.OPERATIONAL && records[recordsI] != SpringState.UNKNOWN) return false
            recordsI++
        }
        for (dgi in 0 until damagedGroup) {
            if (recordsI < records.size && records[recordsI] != SpringState.DAMAGED && records[recordsI] != SpringState.UNKNOWN) return false
            recordsI++
        }
        if (recordsI > records.size) return false
    }
    while(recordsI < records.size) { // check remaining records
        if (records[recordsI] != SpringState.OPERATIONAL && records[recordsI] != SpringState.UNKNOWN) return false
        recordsI++
    }
    return true
}

/*
fun searchPossibleOperationalGroups(records: List<SpringState>, damagedGroups: List<Int>) =
   searchOperationalGroups(damagedGroups, records.size).filter {
       matchGroupsAndRecords(damagedGroups, it, records)
   }


 */
fun searchPossibleOperationalGroups(records: List<SpringState>, damagedGroups: List<Int>): List<List<Int>> {
    val allGroups = searchOperationalGroups(damagedGroups, records.size)
    val result = allGroups.filter {
        matchGroupsAndRecords(damagedGroups, it, records)
    }
    return result
}

/**
 * Method 1: calculate counts for 0 fold and 1 fold, assume that every additional fold will increase by the same factor
 */
fun countPossibleStatesFoldedM1(states: List<SpringState>, groups: List<Int>, foldNr: Int): Long {
    val fold0Count = searchPossibleStates(states, groups).count().toLong()
    val fold1Count = searchPossibleStates(states + listOf(SpringState.UNKNOWN) + states, groups + groups).count().toLong()
    return if (foldNr == 0) fold0Count
    else if (foldNr == 1) fold1Count
    else {
        val foldFact = fold1Count / fold0Count
        var result = fold1Count
        for (i in 2 .. foldNr) result *= foldFact
        result
    }
}

/**
 * Method 2: calculate count for an additional arrangement with a separator and calculate count by multiplying according to the fold nr
 * Special cases: adding the separator at the beginning or the end might lead to different counts.
 * And sometimes count does not increase at all
 */
fun countPossibleStatesFoldedM2(states: List<SpringState>, groups: List<Int>, foldNr: Int): Long {
    val fold0Count = searchPossibleStates(states, groups).count().toLong()
    val fold1Count = searchPossibleStates(states + listOf(SpringState.UNKNOWN) + states, groups + groups).count().toLong()
    if (fold1Count == fold0Count * fold0Count) { // Unknown separator not adding additional arrangements
        var result = fold0Count
        for (i in 2 .. foldNr) result *= fold0Count
        return result
    }
    val foldCountA = searchPossibleStates(listOf(SpringState.UNKNOWN) + states, groups).count().toLong()
    val foldCountB = searchPossibleStates(states + listOf(SpringState.UNKNOWN), groups).count().toLong()
    val foldFact =max(foldCountA, foldCountB)
    var result = fold0Count
    for (i in 1 .. foldNr) result *= foldFact
    return result
}



enum class SpringState { OPERATIONAL, DAMAGED, UNKNOWN }
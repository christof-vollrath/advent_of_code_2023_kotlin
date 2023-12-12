import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe


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
            val springStatesAndGroups = parseSpringStatesAndGroups(exampleInputDay12)
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

    Given("exercise input") {
        val springStatesAndGroups = parseSpringStatesAndGroups(readResource("inputDay12.txt")!!)
        When("Searching possible states") {
            val counts = springStatesAndGroups.map { (states, groups) -> searchPossibleStates(states, groups).count() }
            Then("Summing counts should return right value") {
                counts.sum() shouldBe 8270
            }
        }
    }
} }

fun parseSpringStatesAndGroups(input: String) = input.split("\n").map { line ->
    val parts = line.trim().split(" ")
    val states = parseSpringStates(parts[0])

    val groups = parts[1].split(",").map { it.toInt() }
    states to groups
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


enum class SpringState { OPERATIONAL, DAMAGED, UNKNOWN }
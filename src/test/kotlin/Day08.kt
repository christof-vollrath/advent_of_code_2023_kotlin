import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInput1Day08 = """
    RL
    
    AAA = (BBB, CCC)
    BBB = (DDD, EEE)
    CCC = (ZZZ, GGG)
    DDD = (DDD, DDD)
    EEE = (EEE, EEE)
    GGG = (GGG, GGG)
    ZZZ = (ZZZ, ZZZ)
    """.trimIndent()

val exampleInput2Day08 = """
    LLR
    
    AAA = (BBB, BBB)
    BBB = (AAA, ZZZ)
    ZZZ = (ZZZ, ZZZ)
    """.trimIndent()

val exampleInputPart2Day08 = """
    LR
    
    11A = (11B, XXX)
    11B = (XXX, 11Z)
    11Z = (11B, XXX)
    22A = (22B, XXX)
    22B = (22C, 22C)
    22C = (22Z, 22Z)
    22Z = (22B, 22B)
    XXX = (XXX, XXX)
    """.trimIndent()

class Day08Part1: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input 1") {
            val (instructions1, network1) = parseInstructionsAndNetwork(exampleInput1Day08)
            Then("instructions 1 should be parsed correctly") {
                instructions1 shouldBe listOf('R', 'L')
            }
            Then("network 1 should be parsed correctly") {
                network1.size shouldBe 7
                network1["AAA"] shouldBe Pair("BBB", "CCC")
            }
            val (instructions2, network2) = parseInstructionsAndNetwork(exampleInput2Day08)
            Then("instructions 2 should be parsed correctly") {
                instructions2 shouldBe listOf('L', 'L', 'R')
            }
            Then("network 2 should be parsed correctly") {
                network2.size shouldBe 3
                network2["AAA"] shouldBe Pair("BBB", "BBB")
            }
            When("following instructions 1") {
                val steps = followInstructions(instructions1, network1)
                Then("it should need 2 steps") {
                    steps shouldBe 2
                }
            }
            When("following instructions 2") {
                val steps = followInstructions(instructions2, network2)
                Then("it should need 6 steps") {
                    steps shouldBe 6
                }
            }
        }
    }
    Given("exercise input") {
        val (instructions, network) = parseInstructionsAndNetwork(readResource("inputDay08.txt")!!)
        network.size shouldBe 714
        When("following instruction") {
            followInstructions(instructions, network) shouldBe 22_357
        }
    }
} }

class Day08Part2: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input part 2") {
            val (instructions, network) = parseInstructionsAndNetwork(exampleInputPart2Day08)
            Then("instructions  should be parsed correctly") {
                instructions shouldBe listOf('L', 'R')
            }
            Then("network should be parsed correctly") {
                network.size shouldBe 8
                network["XXX"] shouldBe Pair("XXX", "XXX")
            }
            Then("should have the right starting nodes") {
                filterStartNodes2(network) shouldBe listOf("11A", "22A")
            }
            Then("should be able to decide end correctly") {
                allEndNodes2(listOf("11A", "22A")) shouldBe false
                allEndNodes2(listOf("11Z", "22Z")) shouldBe true
            }
            When("following instructions part 2") {
                val steps = followInstructions2(instructions, network)
                Then("it should need 2, 3 steps") {
                    steps shouldBe listOf(2, 3)
                    lcm(steps.map { it.toLong()} ) shouldBe 6
                }
            }
        }
    }
    Given("exercise input") {
        val (instructions, network) = parseInstructionsAndNetwork(readResource("inputDay08.txt")!!)
        network.size shouldBe 714
        When("following instruction in parallel") {
            lcm(followInstructions2(instructions, network).map { it.toLong()}) shouldBe 10_371_555_451_871L
        }
    }
    Given("some numbers to check least common multiply") {
        lcm(listOf(2,3)) shouldBe 6
        lcm(listOf(4*89, 6*97)) shouldBe 4*3*89*97
    }
} }

fun parseInstructionsAndNetwork(input: String): Pair<List<Char>, Map<String, Pair<String, String>>> {
    val (instructionsChars, networkString) = input.split("\n\n")
    return Pair(parseInstructions(instructionsChars), parseNetwork(networkString))
}

fun parseInstructions(input: String) = input.trim().toCharArray().toList()

fun parseNetwork(input: String) = input.split("\n").associate {
    val (nodeName, connectionString) = it.split(" = ")
    val connections = connectionString.drop(1).dropLast(1).split(", ")
    nodeName to Pair(connections[0], connections[1])
}

fun followInstructions(instructions: List<Char>, network: Map<String, Pair<String, String>>): Int =
    followInstructions("AAA", instructions, network, {name -> name != "ZZZ" } )


fun followInstructions(startNode: String, instructions: List<Char>, network: Map<String, Pair<String, String>>, endCriteria: (String)->Boolean): Int {
    var currentNode = startNode
    var cycles = 0
    var i = 0
    var steps = 0
    while(endCriteria(currentNode)) {
        val node = network[currentNode] ?: throw IllegalArgumentException("Node $currentNode not found")
        currentNode = when(val instruction = instructions[i]) {
            'L' -> node.first
            'R' -> node.second
            else -> throw IllegalArgumentException("Unexpected instruction $instruction")
        }
        i++; steps++
        if (i >= instructions.size) {
            i = 0
            cycles++
        }
        if (cycles >= 1000_000) throw IllegalArgumentException("Too many cycles needed")
    }
    return steps
}

fun filterStartNodes2(network: Map<String, Pair<String, String>>) = network.keys.filter { it.last() == 'A' }
fun allEndNodes2(names: List<String>) = names.all { it.last() == 'Z' }

fun followInstructions2(instructions: List<Char>, network: Map<String, Pair<String, String>>): List<Int> =
    filterStartNodes2(network).map { startNode ->
        followInstructions(startNode, instructions, network, { name -> name.last() != 'Z' } )
    }


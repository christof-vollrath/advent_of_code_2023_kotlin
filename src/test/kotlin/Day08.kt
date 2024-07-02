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

fun followInstructions(instructions: List<Char>, network: Map<String, Pair<String, String>>): Int {
    var currentNode = "AAA"
    var cycles = 0
    var i = 0
    var steps = 0
    while(currentNode != "ZZZ") {
        val node = network[currentNode] ?: throw IllegalArgumentException("Node $currentNode not found")
        val instruction = instructions[i]
        currentNode = when(instruction) {
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


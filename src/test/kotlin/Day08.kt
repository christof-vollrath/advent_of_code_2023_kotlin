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

class Day06Part1: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input") {
            val (instructions, network) = parseInstructionsAndNetwork(exampleInput1Day08)
            Then("instructions should be parsed correctly") {
                instructions shouldBe listOf('R', 'L')
            }
            Then("network should be parsed correctly") {
                network.size shouldBe 7
                network["AAA"] shouldBe Pair("BBB", "CCC")
            }
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


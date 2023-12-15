import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


class Day15Part1: BehaviorSpec() { init {

    Given("simple hash input") {
        val simpleHashInput = "HASH"
        When("calculating hash for simple input") {
            val hash = adventHash(simpleHashInput)
            Then("it should calculate the right value") {
                hash shouldBe 52
            }
        }
    }
    Given("example input") {
        val exampleInput = "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7".split(",")
        When("calculating hash sum") {
            val sum = exampleInput.sumOf { adventHash(it) }
            Then("it should calculate the right sum") {
                sum shouldBe 1320
            }
        }
    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay15.txt")!!.split(",")
        When("calculating hash sum") {
            val sum = exerciseInput.sumOf { adventHash(it) }
            Then("it should calculate the right sum") {
                sum shouldBe 512950
            }
        }
    }
} }

class Day15Part2: BehaviorSpec() { init {

    Given("example input") {
        val exampleInput = "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7"
        When("parsing input") {
            val focalSteps = parseFocalSteps(exampleInput)
            Then("it should parsed steps correctly") {
                focalSteps.size shouldBe 11
                focalSteps[0] shouldBe FocalStep(0, "rn", FocalOperation.UPSERT, 1)
                focalSteps[8] shouldBe FocalStep(3, "pc", FocalOperation.REMOVE,  null)
            }
            When("executing focal operations") {
                val boxes = executeFocalOperations(focalSteps)
                Then("boxes should have right content") {
                    boxes[0] shouldBe mapOf("rn" to FocalLens("rn", 1),
                        "cm" to FocalLens("cm", 2))
                    boxes[3] shouldBe mapOf("ot" to FocalLens("ot", 7),
                        "ab" to  FocalLens("ab", 5),
                        "pc" to FocalLens("pc", 6))
                }
                When("calculating focusing power and summing") {
                    val sum = sumFocusingPower(boxes)
                    Then("sum should be calculated right") {
                        sum shouldBe 145
                    }
                }
            }
        }
    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay15.txt")!!
        val focalSteps = parseFocalSteps(exerciseInput)
        When("executing focal operations and summing focusing power") {
            val boxes = executeFocalOperations(focalSteps)
            val sum = sumFocusingPower(boxes)
            Then("sum should be calculated right") {
                sum shouldBe 247153
            }
        }
    }
} }

fun adventHash(input: String): Int {
    var result = 0
    for (c in input.toCharArray()) {
        result += c.code
        result *= 17
        result %= 256
    }
    return result
}

fun parseFocalSteps(input: String) = input.split(",").map { parseFocalStep(it) }
fun parseFocalStep(input: String ): FocalStep {
    val regex = """([a-z]+)([-=])(\d*)""".toRegex()
    val parts = regex
        .matchEntire(input)
        ?.destructured
        ?.toList()
        ?: throw IllegalArgumentException("Incorrect input line $input")
    val label = parts[0]
    val op = when(parts[1]) {
        "-" -> FocalOperation.REMOVE
        "=" -> FocalOperation.UPSERT
        else -> throw IllegalArgumentException("Operation ${parts[1]}")
    }
    val length = if (parts[2].isNotBlank()) parts[2].toInt() else null
    val hash = adventHash(label)
    return FocalStep(hash, label, op, length)
}

fun executeFocalOperations(focalOperations: List<FocalStep>): Map<Int, LinkedHashMap<String, FocalLens>> {
    val result = mutableMapOf<Int, LinkedHashMap<String, FocalLens>>()
    for (focalOperation in focalOperations) {
        val boxId = focalOperation.hash
        val box = result[boxId]
        val label = focalOperation.label
        val length = focalOperation.length
        when (focalOperation.operation) {
            FocalOperation.UPSERT -> {
                if (box == null) {
                    val createdBox = LinkedHashMap<String, FocalLens>()
                    createdBox[label] = FocalLens(label, length!!)
                    result[boxId] = createdBox
                } else {
                    box[label] = FocalLens(label, length!!)
                }
            }
            FocalOperation.REMOVE -> {
                box?.remove(label)
            }
        }
    }

    return result
}

fun sumFocusingPower(boxes: Map<Int, LinkedHashMap<String, FocalLens>>) =
    (0..255).flatMap { boxId ->
        val box = boxes[boxId]
        val h = box?.values?.mapIndexed { i, focalLens ->
            (boxId + 1) * (i + 1) * focalLens.length
        } ?: listOf(0)
        h
    }.sum()


data class FocalStep(val hash: Int, val label: String, val operation: FocalOperation, val length: Int?)
enum class FocalOperation { REMOVE, UPSERT }
data class FocalLens(val label: String, val length: Int)
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

fun adventHash(input: String): Int {
    var result = 0
    for (c in input.toCharArray()) {
        result += c.code
        result *= 17
        result %= 256
    }
    return result
}
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

val exampleInput = """
    """.trimIndent()

class Day01Part1: BehaviorSpec() { init {
    Given("example input") {
        When("something") {
        }
        Then("anything") {
            5 shouldBe 5
        }
    }
} }

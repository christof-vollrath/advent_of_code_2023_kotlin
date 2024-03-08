import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay19 = """
px{a<2006:qkq,m>2090:A,rfg}
pv{a>1716:R,A}
lnx{m>1548:A,A}
rfg{s<537:gd,x>2440:R,A}
qs{s>3448:A,lnx}
qkq{x<1416:A,crn}
crn{x>2662:A,R}
in{s<1351:px,qqz}
qqz{s>2770:qs,m<1801:hdj,R}
gd{a>3333:R,R}
hdj{m>838:A,pv}

{x=787,m=2655,a=1222,s=2876}
{x=1679,m=44,a=2067,s=496}
{x=2036,m=264,a=79,s=2244}
{x=2461,m=1339,a=466,s=291}
{x=2127,m=1623,a=2188,s=1013}
""".trimIndent()

class Day19Part1: BehaviorSpec() { init {
    Given("a workflow string") {
        val workflowString = "ex{x>10:one,m<20:two,a>30:R,A}"
        When("parsing the workflow") {
            val workflow = parseWorkflowLine(workflowString)
            Then("it should have the right values") {
                workflow.name shouldBe "ex"
                workflow.conditions.size shouldBe 4
                workflow.conditions shouldBe listOf(
                    WorkflowCondition(valName = "x", number = 10, Comparator.GREATER, NamedDestination("one")),
                    WorkflowCondition(valName = "m", number = 20, Comparator.LESS, NamedDestination("two")),
                    WorkflowCondition(valName = "a", number = 30, Comparator.GREATER, RejectDestination),
                    FinalCondition(AcceptDestination)
                )
            }
        }
    }
    Given("a part string") {
        val partString = "{x=787,m=2655,a=1222,s=2876}"
        When("parsing the part") {
            val part = parsePartLine(partString)
            Then("it should be parsed correctly") {
                part shouldBe mapOf(
                    "x" to 787,
                    "m" to 2655,
                    "a" to 1222,
                    "s" to 2876
                )
            }
        }
    }

    Given("workflows and parts") {
        val (workflows, parts) = parseWorkflowsAndParts(exampleInputDay19)
        When("input has been parsed") {
            Then("it should have been parsed correctly") {
                workflows.size shouldBe 11
                workflows[1] shouldBe Workflow("pv", listOf(
                    WorkflowCondition("a", 1716, Comparator.GREATER, RejectDestination),
                    FinalCondition(AcceptDestination)
                ))
                parts.size shouldBe 5
                parts[0]["x"] shouldBe 787
            }
        }
        When("executing the workflow for first part") {
            val (finalDestination, path) = executeWorkflow(workflows, parts[0])
            Then("should have followed the right flow") {
                finalDestination shouldBe AcceptDestination
                path shouldBe listOf("in", "qqz", "qs", "lnx")
            }
        }
        When("executing the workflow for all parts") {
            val result = parts.map { it to executeWorkflow(workflows, it)}
            Then("should have followed the right flows") {
                result.map { it.second.first } shouldBe listOf(AcceptDestination, RejectDestination, AcceptDestination, RejectDestination, AcceptDestination)
            }
            Then("it should have the right sum of accepted parts") {
                val acceptedParts = result.filter { it.second.first == AcceptDestination }.map { it.first }
                acceptedParts.size shouldBe 3
                val sum = acceptedParts.sumOf { it.values.sum() }
                sum shouldBe 19114
            }
        }
    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay19.txt")!!
        When("parsing workflow and parts") {
            val (workflows, parts) = parseWorkflowsAndParts(exerciseInput)
            Then("the right number should be parsed") {
                workflows.size shouldBe 543
                parts.size shouldBe 744 - 543 -1
            }
            When("executing the workflow for all parts") {
                val result = parts.map { it to executeWorkflow(workflows, it)}

                Then("it should have the right sum of accepted parts") {
                    val acceptedParts = result.filter { it.second.first == AcceptDestination }.map { it.first }
                    acceptedParts.size shouldBe 120
                    val sum = acceptedParts.sumOf { it.values.sum() }
                    sum shouldBe 480738
                }
            }
        }
    }

}}

class Day19Part2: BehaviorSpec() { init {

}}

fun parseWorkflowLine(line: String): Workflow {
    val regex = """(\w+)\{(.*)\}""".toRegex()
    val (name, conditionsStr) = regex
        .matchEntire(line)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect input line $line")
    val conditionParts = conditionsStr.split(",")
    val conditions = conditionParts.map { parseCondition(it) }
    return Workflow(name, conditions)
}

fun parseCondition(conditionStr: String) =
    if (conditionStr.contains(":")) parseWorkflowCondition(conditionStr)
    else FinalCondition(parseDestination(conditionStr))

fun parseWorkflowCondition(str: String): WorkflowCondition {
    val (conditionStr, labelStr) = str.split(":")
    val (valName, numberStr) = conditionStr.split("[<>]".toRegex())
    val number = numberStr.toInt()
    val comparator = if (conditionStr.contains("<")) Comparator.LESS else Comparator.GREATER
    return WorkflowCondition(valName, number, comparator, parseDestination(labelStr))
}

fun parseDestination(str: String) = when(str) {
    "A" -> AcceptDestination
    "R" -> RejectDestination
    else -> NamedDestination(str)
}

fun parsePartLine(str: String): Map<String, Int> {
    val partStr = str.drop(1).dropLast(1)
    return partStr.split(",").associate {
        val (name, numberStr) = it.split("=")
        val number = numberStr.toInt()
        name to number
    }
}

fun parseWorkflowsAndParts(input: String): Pair<List<Workflow>, List<Map<String, Int>>> {
    val (workflowsStr, partsStr) = input.split("\n\n")
    val workflows = workflowsStr.split("\n").map { parseWorkflowLine(it) }
    val parts = partsStr.split("\n").map { parsePartLine(it) }
    return Pair(workflows, parts)
}

fun executeWorkflow(workflows: List<Workflow>, part: Map<String, Int>): Pair<AbstractDestination, List<String>> {
    val workflowMap = workflows.associateBy { it.name }
    var currentWorkflowName = "in"
    val path = mutableListOf<String>()
    while (true) {
        path += currentWorkflowName
        val currentWorkflow = workflowMap[currentWorkflowName] ?: throw IllegalArgumentException("Workflow $currentWorkflowName not found")
        loop@ for (condition in currentWorkflow.conditions) {
            when(condition) {
                is FinalCondition -> {
                    when(val destination = condition.destination) {
                        is AcceptDestination   -> return Pair(destination, path)
                        is RejectDestination -> return Pair(destination, path)
                        is NamedDestination -> currentWorkflowName = destination.label
                    }
                }
                is WorkflowCondition -> {
                    val partValue = part[condition.valName] ?: throw IllegalArgumentException("Value ${condition.valName} not found in part")
                    val matches = when (condition.comparator) {
                        Comparator.LESS -> partValue < condition.number
                        Comparator.GREATER -> partValue > condition.number
                    }
                    if (matches) {
                        when(val destination = condition.namedDestination) {
                            is AcceptDestination   -> return Pair(destination, path)
                            is RejectDestination -> return Pair(destination, path)
                            is NamedDestination -> {
                                currentWorkflowName = destination.label
                                break@loop
                            }
                        }
                    }
                }
            }
        }
    }
}


data class Workflow(val name: String, val conditions: List<AbstractWorkflowCondition>)

sealed class AbstractWorkflowCondition
data class FinalCondition(val destination: AbstractDestination): AbstractWorkflowCondition()
data class WorkflowCondition(
    val valName: String,
    val number: Int,
    val comparator: Comparator,
    val namedDestination: AbstractDestination
): AbstractWorkflowCondition()

enum class Comparator { LESS, GREATER }

sealed class AbstractDestination
data object AcceptDestination: AbstractDestination()
data object RejectDestination: AbstractDestination()
data class NamedDestination(val label: String): AbstractDestination()



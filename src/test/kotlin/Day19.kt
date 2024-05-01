/**
 *
--- Day 19: Aplenty ---

The Elves of Gear Island are thankful for your help and send you on your way.
They even have a hang glider that someone stole from Desert Island; since you're already going that direction,
it would help them a lot if you would use it to get down there and return it to them.

As you reach the bottom of the relentless avalanche of machine parts,
you discover that they're already forming a formidable heap.
Don't worry, though - a group of Elves is already here organizing the parts, and they have a system.

To start, each part is rated in each of four categories:

x: Extremely cool looking
m: Musical (it makes a noise when you hit it)
a: Aerodynamic
s: Shiny

Then, each part is sent through a series of workflows that will ultimately accept or reject the part.
Each workflow has a name and contains a list of rules;
each rule specifies a condition and where to send the part if the condition is true.
The first rule that matches the part being considered is applied immediately,
and the part moves on to the destination described by the rule.
(The last rule in each workflow has no condition and always applies if reached.)

Consider the workflow ex{x>10:one,m<20:two,a>30:R,A}.
This workflow is named ex and contains four rules.
If workflow ex were considering a specific part, it would perform the following steps in order:

Rule "x>10:one": If the part's x is more than 10, send the part to the workflow named one.
Rule "m<20:two": Otherwise, if the part's m is less than 20, send the part to the workflow named two.
Rule "a>30:R": Otherwise, if the part's a is more than 30, the part is immediately rejected (R).
Rule "A": Otherwise, because no other rules matched the part, the part is immediately accepted (A).

If a part is sent to another workflow, it immediately switches to the start of that workflow instead and never returns.
If a part is accepted (sent to A) or rejected (sent to R), the part immediately stops any further processing.

The system works, but it's not keeping up with the torrent of weird metal shapes.
The Elves ask if you can help sort a few parts and give you the list of workflows and some part ratings
(your puzzle input).
For example:

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

The workflows are listed first, followed by a blank line,
then the ratings of the parts the Elves would like you to sort.
All parts begin in the workflow named in. In this example, the five listed parts go through the following workflows:

{x=787,m=2655,a=1222,s=2876}: in -> qqz -> qs -> lnx -> A
{x=1679,m=44,a=2067,s=496}: in -> px -> rfg -> gd -> R
{x=2036,m=264,a=79,s=2244}: in -> qqz -> hdj -> pv -> A
{x=2461,m=1339,a=466,s=291}: in -> px -> qkq -> crn -> R
{x=2127,m=1623,a=2188,s=1013}: in -> px -> rfg -> A

Ultimately, three parts are accepted. Adding up the x, m, a, and s rating for each of the accepted parts gives
7540 for the part with x=787, 4623 for the part with x=2036, and 6951 for the part with x=2127.
Adding all of the ratings for all of the accepted parts gives the sum total of 19114.

Sort through all of the parts you've been given;
what do you get if you add together all of the rating numbers for all of the parts that ultimately get accepted?

Your puzzle answer was 480738.

The first half of this puzzle is complete! It provides one gold star: *

--- Part Two ---
Even with your help, the sorting process still isn't fast enough.

One of the Elves comes up with a new plan: rather than sort parts individually through all of these workflows,
maybe you can figure out in advance which combinations of ratings will be accepted or rejected.

Each of the four ratings (x, m, a, s) can have an integer value ranging from a minimum of 1 to a maximum of 4000.
Of all possible distinct combinations of ratings, your job is to figure out which ones will be accepted.

In the above example, there are 167409079868000 distinct combinations of ratings that will be accepted.

Consider only your list of workflows; the list of part ratings that the Elves wanted you to sort is no longer relevant.
How many distinct combinations of ratings will be accepted by the Elves' workflows?

 */
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.max
import kotlin.math.min

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
        When("executing the workflows for first part") {
            val (finalDestination, path) = executeWorkflows(workflows, parts[0])
            Then("should have followed the right flow") {
                finalDestination shouldBe AcceptDestination
                path shouldBe listOf("in", "qqz", "qs", "lnx")
            }
        }
        When("executing the workflows for all parts") {
            val result = parts.map { it to executeWorkflows(workflows, it)}
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
        When("parsing workflows and parts") {
            val (workflows, parts) = parseWorkflowsAndParts(exerciseInput)
            Then("the right number should be parsed") {
                workflows.size shouldBe 543
                parts.size shouldBe 744 - 543 -1
            }
            When("executing the workflows for all parts") {
                val result = parts.map { it to executeWorkflows(workflows, it)}

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
    Given("less comparator") {
        val comparator = Comparator.LESS
        When("applying less on different ranges") {
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(1, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(1, 99)),
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                null,
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 101, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(100, 100)),
                mapOf("a" to PartRange(101, 1000))
            )
            applyComparator("a", 10, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                null,
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                null,
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(1, 10))) shouldBe Pair(
                mapOf("a" to PartRange(1, 10)),
                null
            )
            applyComparator("a", 10, comparator, mapOf("a" to PartRange(1, 10))) shouldBe Pair(
                mapOf("a" to PartRange(1, 9)),
                mapOf("a" to PartRange(10, 10))
            )
            applyComparator("a", 11, comparator, mapOf("a" to PartRange(1, 10))) shouldBe Pair(
                mapOf("a" to PartRange(1, 10)),
                null
            )
        }
    }
    Given("greater comparator") {
        val comparator = Comparator.GREATER
        When("applying greater on different ranges") {
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(1, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(101, 1000)),
                mapOf("a" to PartRange(1, 100))
            )
            applyComparator("a", 99, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(100, 1000)),
                null
            )
            applyComparator("a", 100, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(101, 1000)),
                mapOf("a" to PartRange(100, 100))
            )
            applyComparator("a", 999, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(1000, 1000)),
                mapOf("a" to PartRange(100, 999))
            )
            applyComparator("a", 1010, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                null,
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 1000, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                null,
                mapOf("a" to PartRange(100, 1000))
            )
            applyComparator("a", 10, comparator, mapOf("a" to PartRange(100, 1000))) shouldBe Pair(
                mapOf("a" to PartRange(100, 1000)),
                null
            )
            applyComparator("a", 11, comparator, mapOf("a" to PartRange(10, 100))) shouldBe Pair(
                mapOf("a" to PartRange(12, 100)),
                mapOf("a" to PartRange(10, 11))
            )
            applyComparator("a", 10, comparator, mapOf("a" to PartRange(10, 100))) shouldBe Pair(
                mapOf("a" to PartRange(11, 100)),
                mapOf("a" to PartRange(10, 10))
            )
            applyComparator("a", 9, comparator, mapOf("a" to PartRange(10, 100))) shouldBe Pair(
                mapOf("a" to PartRange(10, 100)),
                null
            )
        }
    }
    Given("a condition with a named destination and ranges") {
        val condition = WorkflowCondition(valName = "a", number = 1716, comparator = Comparator.GREATER, namedDestination = NamedDestination("dest"))
        val partRanges = fullPartRanges
        When("executing condition") {
            val conditionResult = symbolicExecuteCondition(condition, partRanges)
            Then("it should split the ranges") {
                conditionResult.first shouldBe (mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(1717, MAX_RANGE),
                        "s" to PartRange(1, MAX_RANGE)
                    ) to NamedDestination("dest"))
                conditionResult.second shouldBe mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(1, 1716),
                        "s" to PartRange(1, MAX_RANGE)
                    )
            }
        }
    }
    Given("a condition with an reject destination and ranges") {
        val condition = WorkflowCondition(valName = "a", number = 200, comparator = Comparator.LESS, namedDestination = RejectDestination)
        val partRanges = fullPartRanges
        When("executing condition") {
            val conditionResult = symbolicExecuteCondition(condition, partRanges)
            Then("it should split the ranges") {
                conditionResult.first shouldBe (mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(1, 199),
                        "s" to PartRange(1, MAX_RANGE)
                    ) to RejectDestination)
                conditionResult.second shouldBe mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(200, MAX_RANGE),
                        "s" to PartRange(1, MAX_RANGE)
                    )
            }
        }
    }
    Given("a workflow line with only accept"){
        val workflow = parseWorkflowLine("accept{A}")
        When("executing the workflow line symbolically") {
            val partRanges = symbolicExecuteWorkflowLine(workflow, fullPartRanges)
            Then("it should return the part full ranges") {
                partRanges shouldBe Pair(emptyList(), listOf(fullPartRanges))
            }
        }
    }
    Given("a workflow line with only reject"){
        val workflow = parseWorkflowLine("accept{R}")
        When("executing the workflow line symbolically") {
            val partRanges = symbolicExecuteWorkflowLine(workflow, fullPartRanges)
            Then("it should return null as accepted ranges") {
                partRanges shouldBe Pair(emptyList(), emptyList())
            }
        }
    }
    Given("workflow line with accept and reject"){
        val workflow = parseWorkflowLine("pv{a>1716:R,A}")
        When("executing the workflow line symbolically") {
            val partRanges = symbolicExecuteWorkflowLine(workflow, fullPartRanges)
            Then("it should have limited the part ranges") {
                partRanges shouldBe Pair(emptyList(), listOf(mapOf(
                    "x" to PartRange(1, MAX_RANGE),
                    "m" to PartRange(1, MAX_RANGE),
                    "a" to PartRange(1, 1716),
                    "s" to PartRange(1, MAX_RANGE)
                )))
            }
        }
    }
    Given("a workflow line with several accepts and rejects"){
        val workflow = parseWorkflowLine("a{a>1000:R,a<100:A,a>500:R,a<600:A}")
        When("executing the workflow line symbolically") {
            val partRanges = symbolicExecuteWorkflowLine(workflow, fullPartRanges)
            Then("it should return several ranges") {
                partRanges shouldBe Pair(emptyList(),
                    listOf(
                        mapOf(
                            "x" to PartRange(1, MAX_RANGE),
                            "m" to PartRange(1, MAX_RANGE),
                            "a" to PartRange(1, 99),
                            "s" to PartRange(1, MAX_RANGE)
                        ),
                        mapOf(
                            "x" to PartRange(1, MAX_RANGE),
                            "m" to PartRange(1, MAX_RANGE),
                            "a" to PartRange(100, 500),
                            "s" to PartRange(1, MAX_RANGE)
                        )

                    )
                )
            }
        }
    }
    Given("a workflow line which calls another line"){
        val workflow = parseWorkflowLine("a{a>1716:b,A}")
        When("executing the workflow line symbolically") {
            val partRanges = symbolicExecuteWorkflowLine(workflow, fullPartRanges)
            Then("it should have limited the part ranges and return the other destination") {
                partRanges shouldBe Pair(
                    listOf(
                        mapOf(
                            "x" to PartRange(1, MAX_RANGE),
                            "m" to PartRange(1, MAX_RANGE),
                            "a" to PartRange(1717, MAX_RANGE),
                            "s" to PartRange(1, MAX_RANGE)
                        ) to NamedDestination("b")
                    ),
                    listOf(mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(1, 1716),
                        "s" to PartRange(1, MAX_RANGE)
                    ))
                )
            }
        }
    }
    Given("an workflow which accepts everything"){
        val workflows = listOf(parseWorkflowLine("in{A}"))
        When("executing the workflows symbolically") {
            val partRanges = symbolicExecuteWorkflows(workflows, listOf(fullPartRanges))
            Then("it should have the full part ranges as result") {
                partRanges shouldBe listOf(fullPartRanges)
            }
        }
    }
    Given("a simple workflow"){
        val workflows = listOf(parseWorkflowLine("in{a>1716:R,A}"))
        When("executing the workflows symbolically") {
            val partRanges = symbolicExecuteWorkflows(workflows, listOf(fullPartRanges))
            Then("it should have limited the part ranges") {
                partRanges shouldBe listOf(mapOf(
                    "x" to PartRange(1, MAX_RANGE),
                    "m" to PartRange(1, MAX_RANGE),
                    "a" to PartRange(1, 1716),
                    "s" to PartRange(1, MAX_RANGE)
                ))
            }
        }
    }
    Given("a nested workflow"){
        val workflows = listOf(
            parseWorkflowLine("in{a>500:w1,A}"),
            parseWorkflowLine("w1{a>800:R,w2}"),
            parseWorkflowLine("w2{s<700:A,R}")
        )
        When("executing the workflows symbolically") {
            val partRanges = symbolicExecuteWorkflows(workflows, listOf(fullPartRanges))
            Then("it should have limited the part ranges") {
                partRanges shouldBe listOf(
                    mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(1, 500),
                        "s" to PartRange(1, MAX_RANGE)
                    ),
                    mapOf(
                        "x" to PartRange(1, MAX_RANGE),
                        "m" to PartRange(1, MAX_RANGE),
                        "a" to PartRange(501, 800),
                        "s" to PartRange(1, 699)
                    ),
                )
            }
        }
    }
    Given("workflows of example") {
        val (workflows, _) = parseWorkflowsAndParts(exampleInputDay19)
        When("input has been parsed") {
            Then("it should have been parsed correctly") {
                workflows.size shouldBe 11
            }
        }
        When("executing the workflows symbolically") {
            val partRangesList = symbolicExecuteWorkflows(workflows, listOf(fullPartRanges))
            val sum = partRangesList.sumOf { partRanges ->
                partRanges.values.fold(1L, { total, next -> total * (next.to - next.from + 1L) })
            }
            Then("sum should have the right value") {
                sum shouldBe 167_409_079_868_000L
            }
        }
    }
}}

fun parseWorkflowLine(line: String): Workflow {
    val regex = """(\w+)\{(.*)}""".toRegex()
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

fun executeWorkflows(workflows: List<Workflow>, part: Map<String, Int>): Pair<AbstractDestination, List<String>> {
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

fun symbolicExecuteWorkflows(workflows: List<Workflow>, partRangesList: List<PartRanges>): List<PartRanges> {
    val workflowMap = workflows.associateBy { it.name }
    var currentBranches = partRangesList.map { it to NamedDestination("in") }
    val accepted = mutableListOf<PartRanges>()
    while (currentBranches.isNotEmpty()) {
        val nextBranches = mutableListOf<Pair<PartRanges, NamedDestination>>()
        for (currentBranch in currentBranches) {
            val currentWorkflow = workflowMap[currentBranch.second.label] ?: throw IllegalArgumentException("Workflow ${currentBranch.second.label} not found")
            val executionResult = symbolicExecuteWorkflowLine(currentWorkflow, currentBranch.first)
            accepted.addAll(executionResult.second)
            nextBranches.addAll(executionResult.first)
        }
        currentBranches = nextBranches
    }
    return accepted
}

fun symbolicExecuteWorkflowLine(workflow: Workflow, partRanges: PartRanges): Pair<List<Pair<PartRanges, NamedDestination>>, List<PartRanges>> {
    var currentRanges = partRanges
    val branches = mutableListOf<Pair<PartRanges, NamedDestination>>()
    val accepted = mutableListOf<PartRanges>()
    for (condition in workflow.conditions) {
        val result = symbolicExecuteCondition(condition, currentRanges)
        val branch = result.first
        if (branch != null) {
            val destination = branch.second
            when (destination) {
                is NamedDestination -> branches += Pair(branch.first, destination)
                is AcceptDestination -> accepted += branch.first
                is RejectDestination -> {}
            }
        }
        val nextRanges = result.second ?: return Pair(branches, accepted) // break when nothing left
        currentRanges = nextRanges
    }
    accepted += currentRanges
    return Pair(branches, accepted)
}

fun symbolicExecuteCondition(condition: AbstractWorkflowCondition, partRanges: PartRanges):
    Pair<
        Pair<PartRanges, AbstractDestination>?, PartRanges?> =
    when(condition) {
        is WorkflowCondition -> {
            val newRangesPair = applyComparator(condition.valName, condition.number, condition.comparator, partRanges)
            Pair(
                if (newRangesPair.first != null) newRangesPair.first!! to condition.namedDestination else null,
                newRangesPair.second)
        }
        is FinalCondition -> {
            when(val destination = condition.destination) {
                is AcceptDestination -> Pair(null, partRanges)
                is RejectDestination -> Pair(null, null)
                is NamedDestination -> Pair(partRanges to destination, null)
            }
        }
    }


fun applyComparator(valName: String, number: Int, comparator: Comparator, partRanges: Map<String, PartRange>):
    Pair<PartRanges?, PartRanges?> {
    val partRange = partRanges[valName] ?: throw IllegalArgumentException("valName=$valName not found")
    val newPartRange1 = when(comparator) {
        Comparator.LESS -> if (number > partRange.from) PartRange(partRange.from, min(partRange.to, number - 1)) else null
        Comparator.GREATER -> if (number < partRange.to) PartRange(max(partRange.from, number + 1), partRange.to) else null
    }
    val newPartRange2 = when(comparator) {
        Comparator.LESS -> if (number <= partRange.to) PartRange(max(partRange.from, number), partRange.to) else null
        Comparator.GREATER -> if (number >= partRange.from) PartRange(partRange.from, min(partRange.to, number)) else null
    }
    val newPartRanges1 = if (newPartRange1 != null) partRanges.mapValues { if (it.key == valName) newPartRange1 else it.value}
        else null
    val newPartRanges2 = if (newPartRange2 != null) partRanges.mapValues { if (it.key == valName) newPartRange2 else it.value}
    else null
    return Pair(newPartRanges1, newPartRanges2)
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

data class PartRange(val from: Int, val to: Int)
typealias PartRanges = Map<String, PartRange>

val MAX_RANGE = 4000
val fullPartRanges: Map<String, PartRange> = mapOf(
    "x" to PartRange(1, MAX_RANGE),
    "m" to PartRange(1, MAX_RANGE),
    "a" to PartRange(1, MAX_RANGE),
    "s" to PartRange(1, MAX_RANGE)
)

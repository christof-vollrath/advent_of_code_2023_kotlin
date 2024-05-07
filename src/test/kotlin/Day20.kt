import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val exampleInputDay20simple = """
broadcaster -> a, b, c
%a -> b
%b -> c
%c -> inv
&inv -> a""".trimIndent()

val exampleInputDay20interesting = """
broadcaster -> a
%a -> inv, con
&inv -> b
%b -> con
&con -> output""".trimIndent()

class Day20Part1: BehaviorSpec() { init {
    Given("the simple example module configuration") {
        val (moduleConfiguration, moduleMap) = parseModuleConfiguration(exampleInputDay20simple)
        When("configuration") {
            Then("it should have parsed the right configuration") {
                val broadcaster = BroadCast("broadcaster")
                val a = FlipFlop("a")
                val b = FlipFlop("b")
                val c = FlipFlop("c")
                val inv = Conjunction("inv")
                broadcaster.connectedTo = listOf(a, b, c)
                a.connectedTo = listOf(b)
                b.connectedTo = listOf(c)
                c.connectedTo = listOf(inv)
                inv.connectedTo = listOf(a)
                broadcaster.connectedFrom = emptyList()
                a.connectedFrom = listOf(broadcaster, inv)
                b.connectedFrom = listOf(broadcaster, a)
                c.connectedFrom = listOf(broadcaster, c)
                c.connectedFrom = listOf(broadcaster, c)
                inv.connectedFrom = listOf(broadcaster, c)

                moduleConfiguration shouldBe broadcaster
                moduleConfiguration.connectedTo.map { it.name } shouldBe listOf("a", "b", "c")
                moduleConfiguration.connectedFrom shouldBe listOf()


                val parsedC = moduleMap["c"]
                parsedC.shouldNotBeNull()
                parsedC.connectedTo.map { it.name } shouldBe listOf("inv")
                parsedC.connectedFrom shouldBe listOf(broadcaster, b)

                val parsedInv = moduleMap["inv"]
                parsedInv.shouldNotBeNull()
                parsedInv.connectedTo.map { it.name } shouldBe listOf("a")
                parsedInv.connectedFrom shouldBe listOf(c)
            }
        }
        When("pushing the button on the simple example") {
            val simulationState = simulatePushButton(moduleMap)
            Then("inv should be high") {
                simulationState.moduleStates.getModuleState("a") shouldBe 0
                simulationState.moduleInputStates.getInputState("a", "inv") shouldBe 1
                simulationState.simulationStatistics.nrLowPulsesSent shouldBe 8 // Including button
                simulationState.simulationStatistics.nrHighPulsesSent shouldBe 4
            }
            When("pushing it again, same events should happen") {
                simulatePushButton(moduleMap, simulationState)
                Then("inv should be high") {
                    simulationState.moduleStates.getModuleState("a") shouldBe 0
                    simulationState.moduleInputStates.getInputState("a", "inv") shouldBe 1
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 16 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 8
                }
            }
        }
    }
    Given("A state of flip flops") {
        val flipFlopStates: ModuleStates = mutableMapOf()

        When("getting a state of an uninitialized flip flop") {
            flipFlopStates.getModuleState("a") shouldBe 0
        }
        When("setting a state") {
            flipFlopStates.setModuleState("a", 1)
            Then("state should have changed") {
                flipFlopStates.getModuleState("a") shouldBe 1
            }
        }
    }
    Given("A state of module inputs") {
        val inputStates: ModuleInputStates = mutableMapOf()

        When("getting a state of an uninitialized input") {
            inputStates.getInputState("a", "b") shouldBe 0
        }
        When("getting all states of an uninitialized input") {
            inputStates.getInputStates("a") shouldBe emptyList()
        }
        When("setting a state") {
            inputStates.setInputState("a", "b", 1)
            Then("state should have changed") {
                inputStates.getInputState("a", "b") shouldBe 1
                inputStates.getInputStates("a") shouldBe listOf(1)
            }
        }
    }
    Given("A broadcast and a conjunction module with one input") {
        val broadcaster = BroadCast("broadcaster")
        val a = Conjunction("a")
        val b= Conjunction("b")
        broadcaster.connectedTo = listOf(a)
        a.connectedTo = listOf(b)
        broadcaster.connectedFrom = emptyList()
        a.connectedFrom = listOf(broadcaster)
        b.connectedFrom = listOf(b)
        When("pushing the button") {
            val (_, moduleInputStates, statistics) = simulatePushButton(mapOf("broadcaster" to broadcaster, "a" to a, "b" to b))
            Then("input for module connected to 'a' should be high and statistics should be right") {
                moduleInputStates.getInputState("b", "a") shouldBe 1
                statistics.nrLowPulsesSent shouldBe 2 // Including button
                statistics.nrHighPulsesSent shouldBe 1
            }
        }
    }
    Given("A broadcast and a flip flop module") {
        val broadcaster = BroadCast("broadcaster")
        val a = FlipFlop("a")
        val b= Conjunction("b")
        broadcaster.connectedTo = listOf(a)
        a.connectedTo = listOf(b)
        broadcaster.connectedFrom = emptyList()
        a.connectedFrom = listOf(broadcaster)
        b.connectedFrom = listOf(b)
        val moduleMap = mapOf("broadcaster" to broadcaster, "a" to a, "b" to b)
        When("pushing the button") {
            val simulationState = simulatePushButton(moduleMap)
            Then("'a' should flip, input for module connected to 'a' should be high") {
                simulationState.moduleInputStates.getInputState("b", "a") shouldBe 1
                simulationState.simulationStatistics.nrLowPulsesSent shouldBe 2 // Including button
                simulationState.simulationStatistics.nrHighPulsesSent shouldBe 1
            }
            When("pushing the button again") {
                simulatePushButton(moduleMap, simulationState)
                Then("'a' should flip again, input for module connected to 'a' should be low again") {
                    simulationState.moduleInputStates.getInputState("b", "a") shouldBe 0
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 5 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 1
                }
            }
        }
    }
    Given("the more interesting example") {
        val (_, moduleMap) = parseModuleConfiguration(exampleInputDay20interesting)
        When("pushing the button on the interesting example") {
            val simulationState = simulatePushButton(moduleMap)
            Then("states should be right") {
                simulationState.moduleStates.getModuleState("a") shouldBe 1
                simulationState.moduleStates.getModuleState("b") shouldBe 1
                simulationState.moduleInputStates.getInputState("con", "output") shouldBe 0
                simulationState.simulationStatistics.nrLowPulsesSent shouldBe 4 // Including button
                simulationState.simulationStatistics.nrHighPulsesSent shouldBe 4
            }
            When("pushing it again, other events should occur") {
                simulatePushButton(moduleMap, simulationState)
                Then("states should be right") {
                    simulationState.moduleStates.getModuleState("a") shouldBe 0
                    simulationState.moduleStates.getModuleState("b") shouldBe 1
                    simulationState.moduleInputStates.getInputState("output", "con") shouldBe 1
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 8 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 6
                }
            }
            When("pushing it a third time, more events should occur") {
                simulatePushButton(moduleMap, simulationState)
                Then("states should be right") {
                    simulationState.moduleStates.getModuleState("a") shouldBe 1
                    simulationState.moduleStates.getModuleState("b") shouldBe 0
                    simulationState.moduleInputStates.getInputState("output", "con") shouldBe 1
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 13 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 9
                }
            }
            When("pushing it a forth time, more events should occur") {
                simulatePushButton(moduleMap, simulationState)
                Then("states should be right") {
                    simulationState.moduleStates.getModuleState("a") shouldBe 0
                    simulationState.moduleStates.getModuleState("b") shouldBe 0
                    simulationState.moduleInputStates.getInputState("output", "con") shouldBe 1
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 17 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 11
                }
            }
            When("pushing it in total 1000 times") {
                repeat(1000-4) { simulatePushButton(moduleMap, simulationState) }

                Then("statistics should be right") {
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 4250 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 2750
                }
            }
        }
    }

} }

fun  ModuleStates.getModuleState(name: String) = getOrDefault(name, 0)
fun  ModuleStates.setModuleState(name: String, value: Int) = put(name, value)

fun  ModuleInputStates.getInputState(moduleName: String, inputName: String): Int {
    val inputMap = get(moduleName)
    return inputMap?.getOrDefault(inputName, 0) ?: 0
}
fun ModuleInputStates.getInputStates(moduleName: String): List<Int> {
    val inputMap = get(moduleName)
    return inputMap?.values?.toList() ?: return emptyList()
}
fun  ModuleInputStates.setInputState(moduleName: String, inputName: String, value: Int) {
    val inputMap = get(moduleName)
    if (inputMap == null) put(moduleName, mutableMapOf(inputName to value))
    else inputMap[inputName] = value
}

data class SimulationStatistics(var nrLowPulsesSent: Int = 0, var nrHighPulsesSent: Int = 0)

fun parseModuleConfiguration(input: String): Pair<BroadCast, Map<String, Module>> {
    val modulesAndConnections = input.split("\n").map { parseModule(it.trim())}
    val moduleMap = modulesAndConnections.map { it.first }.associateBy { it.name  }
    // find unconnected test modules
    val testModulesAndConnections = sequence {
        for ((_, connections) in modulesAndConnections) {
            for (connectedTo in connections) {
                val module = moduleMap[connectedTo]
                if (module == null) {
                    yield(Pair(TestModule(connectedTo), emptyList<String>())) // special test module not with a line
                }
            }
        }
    }.toList()
    val extendedModulesAndConnections = modulesAndConnections + testModulesAndConnections
    val extendedModuleMap = extendedModulesAndConnections.map { it.first }.associateBy { it.name  }
    for ((module, connections) in extendedModulesAndConnections) { // set connectedTo
        val connectedTo = connections.mapNotNull { extendedModuleMap[it]}
        module.connectedTo = connectedTo
    }
    val modules = extendedModulesAndConnections.map { it.first }
    for (module in modules) { // set connectedFrom
        module.connectedFrom = modules.filter { it.connectedTo.contains(module)}
    }
    val result =  extendedModuleMap["broadcaster"] ?: throw IllegalArgumentException("No broadcaster found")
    if (result !is BroadCast) throw IllegalArgumentException("broadcaster is not a broadcaster")
    return Pair(result, extendedModuleMap)
}

fun parseModule(inputLine: String): Pair<Module, List<String>> {
    val (modulePart, connections) = inputLine.split("->").map { it.trim() }

    val module = if (modulePart == "broadcaster") BroadCast(modulePart)
    else {
        val prefix = modulePart[0]
        val name = modulePart.drop(1)
        when(prefix) {
            '%' -> FlipFlop(name)
            '&' -> Conjunction(name)
            else -> throw IllegalArgumentException("Unexpected prefix $prefix")
        }
    }
    val connectionList = connections.split(",").map { it.trim()}
    return  module to connectionList
}

fun simulatePushButton(moduleMap: Map<String, Module>, simulationState: SimulationState = SimulationState()): SimulationState {

    fun sendToConnected(module: Module, value: Int) : List<Pulse> {
        val generatedPulses = mutableListOf<Pulse>()
        for (connection in module.connectedTo) {
            simulationState.moduleInputStates.setInputState(connection.name, module.name, value)
            generatedPulses.add(Pulse(value, connection.name))
            if (value == 0)
                simulationState.simulationStatistics.nrLowPulsesSent++
            else
                simulationState.simulationStatistics.nrHighPulsesSent++

        }
        return generatedPulses
    }

    simulationState.simulationStatistics.nrLowPulsesSent++
    var currentPulses =  mutableListOf(Pulse(0, "broadcaster"))
    while (currentPulses.isNotEmpty()) {
        val nextPulses = mutableListOf<Pulse>()
        for (pulse in currentPulses) {
            val currentModule = moduleMap[pulse.to] ?: throw IllegalArgumentException("Module $pulse not found")
            when(currentModule) {
                is BroadCast -> {
                    nextPulses += sendToConnected(currentModule, 0) // Broadcaster sends always 0 (low)
                    currentModule.connectedTo.map { it.name }
                }
                is Conjunction -> {
                    val inputValues = currentModule.connectedFrom.map {
                        simulationState.moduleInputStates.getInputState(currentModule.name, it.name)
                    } // conjunction uses stored input values
                    val conjResult = if (inputValues.all { it == 1 }) 0 else 1
                    nextPulses += sendToConnected(currentModule, conjResult)
                }
                is FlipFlop -> {
                    val currentValue = simulationState.moduleStates.getModuleState(currentModule.name)
                    val flips = pulse.value == 0
                    val nextStateValue = if (flips)
                        if (currentValue == 0) 1
                        else 0
                    else currentValue
                    if (flips) {
                        simulationState.moduleStates.setModuleState(currentModule.name, nextStateValue)
                        nextPulses += sendToConnected(currentModule, nextStateValue)
                    }
                }
            }
        }
        currentPulses = nextPulses
    }
    return simulationState
}

abstract class Module {
    abstract val name: String
    var connectedTo: List<Module> = emptyList()
    var connectedFrom: List<Module> = emptyList()
}
class Conjunction(override val name: String) : Module()
data class FlipFlop(override val name: String) : Module()
data class BroadCast(override val name: String) : Module()
data class TestModule(override val name: String) : Module()

data class Pulse(val value: Int, val to: String)

data class SimulationState(val moduleStates: ModuleStates = mutableMapOf(),
    val moduleInputStates: ModuleInputStates = mutableMapOf(),
    val simulationStatistics: SimulationStatistics = SimulationStatistics())
typealias ModuleStates = MutableMap<String, Int>
typealias ModuleInputStates = MutableMap<String, MutableMap<String, Int>>
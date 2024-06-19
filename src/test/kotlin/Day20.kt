import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

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

class Day20Part1 : BehaviorSpec() { init {
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
    Given("exercise input") {
        val exerciseInput = readResource("inputDay20.txt")!!
        When("parsing module map") {
            val (_, moduleMap) = parseModuleConfiguration(exerciseInput)
            Then("the right number should be parsed") {
                moduleMap.size shouldBe 58 + 1 // One test module 'x'
            }
            When("pushing the button 1000 times") {
                val simulationState = SimulationState()
                repeat(1000) { simulatePushButton(moduleMap, simulationState) }
                Then("statistics should be right") {
                    simulationState.simulationStatistics.nrLowPulsesSent shouldBe 18829 // Including button
                    simulationState.simulationStatistics.nrHighPulsesSent shouldBe 46016
                    simulationState.simulationStatistics.nrLowPulsesSent *
                            simulationState.simulationStatistics.nrHighPulsesSent shouldBe 866435264
                }

            }
        }
    }

} }

class Day20Part2 : BehaviorSpec() { init {
    Given("the more interesting example") {
        val (_, moduleMap) = parseModuleConfiguration(exampleInputDay20interesting)
        When("pushing the button until inv gets a low pulse") {
            val nr = pushButtonUntil(moduleMap, { it.moduleInputStates.getInputState("b", "inv") == 1 })
            Then("nr of button pushes should be two") {
                nr shouldBe 2
            }
        }
    }
    Given("exercise input") {
        val exerciseInput = readResource("inputDay20.txt")!!
        When("parsing module map") {
            val (_, moduleMap) = parseModuleConfiguration(exerciseInput)
            Then("the right number should be parsed") {
                moduleMap.size shouldBe 58 + 1 // One test module 'x'
            }
            Then("rx should have the right type and connections") {
                val rx = moduleMap["rx"]
                rx.shouldBeInstanceOf<TestModule>()
                rx.connectedFrom.map { it.name } shouldBe listOf("dg")
            }
            Then("dg should have the right type and connections") {
                val dg = moduleMap["dg"]
                dg.shouldBeInstanceOf<Conjunction>()
                dg.connectedFrom.map { it.name } shouldBe listOf("lk", "zv", "sp", "xt")
            }
            Then("lk should have the right type and connections") {
                val lk = moduleMap["lk"]
                lk.shouldBeInstanceOf<Conjunction>()
                lk.connectedFrom.map { it.name } shouldBe listOf("jc")
            }
            Then("zv should have the right type and connections") {
                val zv = moduleMap["zv"]
                zv.shouldBeInstanceOf<Conjunction>()
                zv.connectedFrom.map { it.name } shouldBe listOf("vv")
            }
            Then("sp should have the right type and connections") {
                val sp = moduleMap["sp"]
                sp.shouldBeInstanceOf<Conjunction>()
                sp.connectedFrom.map { it.name } shouldBe listOf("xq")
            }
            Then("xt should have the right type and connections") {
                val xt = moduleMap["xt"]
                xt.shouldBeInstanceOf<Conjunction>()
                xt.connectedFrom.map { it.name } shouldBe listOf("dv")
            }
            Then("jc should have the right type and connections") {
                val jc = moduleMap["jc"]
                jc.shouldBeInstanceOf<Conjunction>()
                jc.connectedFrom.map { it.name } shouldBe listOf("kz", "mc", "tx", "mg", "cd", "xc", "tp", "hl", "qb", "rv")
            }
            Then("vv should have the right type and connections") {
                val vv = moduleMap["vv"]
                vv.shouldBeInstanceOf<Conjunction>()
                vv.connectedFrom.map { it.name } shouldBe listOf("sg", "xx", "lc", "vh", "cc", "vn", "jq", "kx", "cz")
            }
            Then("xq should have the right type and connections") {
                val xq = moduleMap["xq"]
                xq.shouldBeInstanceOf<Conjunction>()
                xq.connectedFrom.map { it.name } shouldBe listOf("hg", "gt", "ln", "nt", "nj", "mq", "sl", "pt")
            }
            Then("dv should have the right type and connections") {
                val dv = moduleMap["dv"]
                dv.shouldBeInstanceOf<Conjunction>()
                dv.connectedFrom.map { it.name } shouldBe listOf("vp", "nv", "td", "dm", "rc", "gf", "rm", "jt", "dc")
            }
            When("waiting for rx going to 0") {
                val nr = pushButtonUntil(moduleMap, { it.moduleInputStates.getInputState("rx", "dg") == 0 })
                Then("should take two long") {
                    nr shouldBe -1
                }
            }
            When("checking for lk->dg high") {
                val nrs = findPushNrsWhere(moduleMap, { it.moduleInputStates.getInputState("dg", "lk") == 1 })
                Then("it should find push nrs") {
                    nrs shouldBe emptyList()
                }
            }
            When("checking for zv->dg high") {
                val nrs = findPushNrsWhere(moduleMap, { it.moduleInputStates.getInputState("dg", "zv") == 1 })
                Then("it should find push nrs") {
                    nrs shouldBe emptyList()
                }
            }
            /*
            When("finding changes in xq") {
                val simulationState = SimulationState()
                for (nr in 1..1000) {
                    simulatePushButton(moduleMap, simulationState)
                    val h1 = listOf("hg", "gt", "ln", "nt", "nj", "mq", "sl", "pt").map { from ->
                        simulationState.moduleInputStates.getInputState("xq", from)
                    }
                    println(h1)
                    val h2 = simulationState.moduleInputStates.getInputState("sp", "xq")
                    println(h2)
                    if (h2 != 1) println("!!!!")
                }
            }

             */
            When("finding all modules which lead to rx transitively") {
                val rx = moduleMap["rx"]!!
                val transientConntectedFrom = findTransientConnectedFrom(rx)
                Then("it will find all the modules execpt rx because everything is connected to it") {
                    transientConntectedFrom.size shouldBe moduleMap.size - 1
                }
            }
            When("finding all modules which lead to jc transitively") {
                val rx = moduleMap["jc"]!!
                val transientConntectedFrom = findTransientConnectedFrom(rx)
                Then("it will find only some modulest") {
                    transientConntectedFrom.size shouldBe 13
                }
                When("creating a reduced module map") {
                    val reducedMap = reduceConnectedTo(transientConntectedFrom + listOf(rx))
                    Then("it should still have all modules") {
                        reducedMap.size shouldBe 14
                    }
                    When("checking for jc->mg low") {
                        val simulationState = SimulationState()
                        for (nr in 1..10_000) {
                            simulatePushButton(reducedMap, simulationState)
                            val h1 = listOf("kz", "mc", "tx", "mg", "cd", "xc", "tp", "hl", "qb", "rv", "xv", "ps").map { from ->
                                simulationState.moduleInputStates.getInputState("jc", from)
                            }
                            println(h1)
                            val h2 = simulationState.moduleInputStates.getInputState("mg", "jc")
                            println("$nr $h2")
                            if (h2 != 1) println("!!!! + $nr")
                        }
                    }

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
                is TestModule -> {}
            }
        }
        currentPulses = nextPulses
    }
    return simulationState
}

fun pushButtonUntil(moduleMap: Map<String, Module>, check: (SimulationState) -> Boolean, maxRepeats: Int = 10_000): Int {
    val simulationState = SimulationState()
    var nr = 0
    do {
        if (nr >= maxRepeats) return -1
        simulatePushButton(moduleMap, simulationState)
        nr++
    } while (!check(simulationState))
    return nr
}

fun findPushNrsWhere(moduleMap: Map<String, Module>, check: (SimulationState) -> Boolean, maxRepeats: Int = 10_000): List<Int> {
    val simulationState = SimulationState()
    var nr = 0
    val result = mutableListOf<Int>()
    while (nr <= maxRepeats){
        simulatePushButton(moduleMap, simulationState)
        if (check(simulationState)) result += nr
        nr++
    }
    return result
}

fun findTransientConnectedFrom(startModule: Module): List<Module> {
    val result = mutableListOf<Module>()
    val visited = mutableSetOf<Module>(startModule)
    var currentModules = listOf(startModule)
    while(currentModules.isNotEmpty()) {
        val nextModules = mutableListOf<Module>()
        for (currentModule in currentModules) {
            val connectedFrom = currentModule.connectedFrom
            val notYetVisited = connectedFrom.filter { ! visited.contains(it) }
            visited.addAll(notYetVisited)
            nextModules.addAll(notYetVisited)
            result.addAll(notYetVisited)
            println("${currentModule.name} ${currentModule.javaClass} <- ${connectedFrom.map{it.name}}")
        }
        currentModules = nextModules
    }
    return result
}

private fun reduceConnectedTo(modules: List<Module>): Map<String, Module> {
    fun filterConnectedTo(modules: List<Module>, all: Set<Module>) = modules.filter { all.contains(it )}

    val allModules = modules.toSet()
    val reducedModules = modules.map { module ->
        val result = when(module) {
            is Conjunction -> Conjunction(module.name)
            is FlipFlop -> FlipFlop(module.name)
            is BroadCast -> BroadCast(module.name)
            is TestModule -> TestModule(module.name)
        }
        result.connectedFrom = module.connectedFrom
        result.connectedTo = filterConnectedTo(module.connectedTo, allModules)
        result
    }
    return reducedModules.associateBy( { it.name } )
}

sealed class Module {
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
    val simulationStatistics: SimulationStatistics = SimulationStatistics(),
    var lowPulseFound: Boolean = false)
typealias ModuleStates = MutableMap<String, Int>
typealias ModuleInputStates = MutableMap<String, MutableMap<String, Int>>
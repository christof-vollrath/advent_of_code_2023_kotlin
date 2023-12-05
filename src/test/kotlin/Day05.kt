import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


val exampleInputDay05 = """
        seeds: 79 14 55 13
        
        seed-to-soil map:
        50 98 2
        52 50 48
        
        soil-to-fertilizer map:
        0 15 37
        37 52 2
        39 0 15
        
        fertilizer-to-water map:
        49 53 8
        0 11 42
        42 0 7
        57 7 4
        
        water-to-light map:
        88 18 7
        18 25 70
        
        light-to-temperature map:
        45 77 23
        81 45 19
        68 64 13
        
        temperature-to-humidity map:
        0 69 1
        1 0 69
        
        humidity-to-location map:
        60 56 37
        56 93 4
    """.trimIndent()

class Day05Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val plantAlmanac = parsePlantAlmanac(exampleInputDay05)
            Then("seeds should be parsed correctly") {
                plantAlmanac.seeds shouldBe setOf(79L, 14L, 55L, 13L)
            }
            Then("first map should be parsed correctly") {
                val firstMap = plantAlmanac.maps["seed"]!!
                firstMap.from shouldBe "seed"
                firstMap.to shouldBe "soil"
                firstMap.mapRanges[0].destinationRange shouldBe 50
                firstMap.mapRanges[0].sourceRange shouldBe 98
                firstMap.mapRanges[0].rangLength shouldBe 2
            }
            Then("it should have found 7 maps in total") {
                plantAlmanac.maps.size shouldBe 7
            }
            When("going through all maps for seed 79") {
                val result = goThroughAllMaps(plantAlmanac.maps, 79L)
                Then("it should find the right location") {
                    result shouldBe 82L
                }
            }
            When("finding the soil for all seeds") {
                val result = plantAlmanac.seeds.map { goThroughAllMaps(plantAlmanac.maps, it) }
                Then("it should find the right locations") {
                    result shouldBe listOf(82L, 43L, 86L, 35L)
                }
                Then("it should have found the smallest location") {
                    result.min() shouldBe 35
                }
            }
        }
    }

    Given("exercise input") {
        val plantAlmanac = parsePlantAlmanac(readResource("inputDay05.txt")!!)
        When("going through all seeds and maps and finding min location") {
            val result = plantAlmanac.seeds.minOfOrNull { goThroughAllMaps(plantAlmanac.maps, it) }
            Then("it should have found the right location") {
                result shouldBe 174137457L
            }
        }
    }
}

    private fun goThroughAllMaps(maps: Map<String, PlantMap>, id: Long, start: String = "seed"): Long {
        fun mapId(plantMap: PlantMap, id: Long): Long {
            for (mapRange in plantMap.mapRanges) {
                if (id in mapRange.sourceRange until mapRange.sourceRange+mapRange.rangLength)
                    return mapRange.destinationRange + id - mapRange.sourceRange
            }
            return id // Default for no mapping
        }
        var currMapName: String? = start
        var currId = id
        while(true) {
            val currMap = maps[currMapName] ?: break
            currId = mapId(currMap, currId)
            currMapName = currMap.to
        }
        return currId
    }

}

fun parsePlantAlmanac(input: String): PlantAlmanac {
    val lines = input.split("\n")
    val seedLine = lines.first()
    val mapsLines = lines.drop(2)
    val maps = splitAndParseMaps(mapsLines)
    val mapsByFrom = maps.associateBy { it.from }
    val seeds = parseSeedLine(seedLine)

    return PlantAlmanac(seeds, mapsByFrom)
}

fun parseSeedLine(line: String): Set<Long> {
    val seedLineRegex = """seeds:([ \d]+)""".toRegex()
    val (seedsString) = seedLineRegex
        .matchEntire(line)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect input line $line")
    return seedsString.split(" ").filter { it.isNotBlank() }.map { it.trim().toLong() }.toSet()
}

fun splitAndParseMaps(lines: List<String>) = buildList {
    var mapLines = mutableListOf<String>()
    lines.forEach { line ->
        if (line.isBlank()) { // start of a new amp
            add(parseMap(mapLines))
            mapLines = mutableListOf()
        } else
            mapLines += line
    }
    add(parseMap(mapLines))
}

fun parseMap(lines: List<String>): PlantMap {
    val headLine = lines.first()
    val mapHeaderRegex = """(\w+)-to-(\w+) map:""".toRegex()
    val (from, to) = mapHeaderRegex
        .matchEntire(headLine)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect map head line $headLine")
    val mapLines = lines.drop(1)
    val mapRanges = mapLines.map { line ->
        val (destinationRange, sourceRange, rangeLength) = line.split(" +".toRegex()).map { it.toLong() }
        MapRange(destinationRange, sourceRange, rangeLength)
    }
    return PlantMap(from, to, mapRanges)
}

data class MapRange(val destinationRange: Long, val sourceRange: Long, val rangLength: Long)
data class PlantMap(val from: String, val to: String, val mapRanges: List<MapRange>)
data class PlantAlmanac(val seeds: Set<Long>, val maps: Map<String, PlantMap>)

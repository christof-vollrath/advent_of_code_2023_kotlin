import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.min


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
} }

class Day05Part2: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val plantAlmanac = parsePlantAlmanac2(exampleInputDay05)
            Then("seeds should be parsed correctly") {
                plantAlmanac.seeds shouldBe setOf(PlantRange(79L, 14L), PlantRange(55L, 13L))
            }
            Then("first map should be parsed correctly") {
                val firstMap = plantAlmanac.maps["seed"]!!
                firstMap.from shouldBe "seed"
                firstMap.to shouldBe "soil"
                firstMap.mapRanges[0].destinationRange shouldBe 50
                firstMap.mapRanges[0].sourceRange shouldBe 98
                firstMap.mapRanges[0].rangLength shouldBe 2
            }
            When("going through all maps for seed range 79, 14") {
                val result = goThroughAllMaps2(plantAlmanac.maps, setOf(PlantRange(79L, 14L)))
                Then("it should find the right location") {
                    result shouldBe setOf(PlantRange(46, 10), PlantRange(60, 1), PlantRange(82, 3))
                }
            }
            When("going through all maps for all seed ranges") {
                val result = goThroughAllMaps2(plantAlmanac.maps, plantAlmanac.seeds)
                Then("it should find the right location") {
                    result.minOf { it.start } shouldBe 46
                }
            }
        }
    }
    Given("exercise input") {
        val plantAlmanac = parsePlantAlmanac2(readResource("inputDay05.txt")!!)
        When("going through all seeds and maps and finding min location") {
            val ranges = goThroughAllMaps2(plantAlmanac.maps, plantAlmanac.seeds)
            Then("it should have found the right location") {
                ranges.minOf { it.start } shouldBeLessThan  4249618L
                ranges.minOf { it.start } shouldBe  1493866
            }
        }
    }
} }

class Day05Part2FindOverlappingRanges : FunSpec({
    val plantMap = PlantMap("seed", "soil", listOf(
        MapRange(0, 50, 10)
    ))
    context("test mapping for different ranges") {
        withData(
            Pair(PlantRange(10, 10), setOf(PlantRange(10, 10))), // not overlapping
            Pair(PlantRange(40, 10), setOf(PlantRange(40, 10))), // still not overlapping
            Pair(PlantRange(40, 11), setOf(PlantRange(40, 10), PlantRange(0, 1))), // one overlapping and mapped
            Pair(PlantRange(80, 10), setOf(PlantRange(80, 10))), // not overlapping at the end
            Pair(PlantRange(60, 10), setOf(PlantRange(60, 10))), // still not overlapping
            Pair(PlantRange(59, 11), setOf(PlantRange(60, 10), PlantRange(9, 1))), // one overlapping and mapped
            Pair(PlantRange(50, 10), setOf(PlantRange(0, 10))), // exactly match
            Pair(PlantRange(51, 5), setOf(PlantRange(1, 5))), // partly match
            Pair(PlantRange(45, 20), setOf(PlantRange(45, 5), PlantRange(0, 10), PlantRange(60, 5))), // overlapping on both sides
        ) { (range, expected) ->
            val ranges = rangeGoThroughMap(plantMap, range)
                ranges shouldBe expected
        }
    }
})

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


fun parsePlantAlmanac2(input: String): PlantAlmanac2 {
    val lines = input.split("\n")
    val seedLine = lines.first()
    val mapsLines = lines.drop(2)
    val maps = splitAndParseMaps(mapsLines)
    val mapsByFrom = maps.associateBy { it.from }
    val seeds = parseSeedLine2(seedLine)

    return PlantAlmanac2(seeds, mapsByFrom)
}

fun parseSeedLine2(line: String): Set<PlantRange> {
    val seedLineRegex = """seeds:([ \d]+)""".toRegex()
    val (seedsString) = seedLineRegex
        .matchEntire(line)
        ?.destructured
        ?: throw IllegalArgumentException("Incorrect input line $line")
    return seedsString.split(" ").asSequence().filter { it.isNotBlank() }
        .map { it.trim().toLong() }
        .chunked(2)
        .map { (start, len ) -> PlantRange(start, len) }
        .toSet()
}

data class PlantAlmanac2(val seeds: Set<PlantRange>, val maps: Map<String, PlantMap>)

data class PlantRange(val start: Long, val len: Long)

private fun rangeGoThroughMap(map: PlantMap, plantRange: PlantRange): Set<PlantRange> {
    var currUnmappedRanges = setOf(plantRange)
    val mappedRanges = mutableSetOf<PlantRange>()
    for (mapRange in map.mapRanges) {
        val nextUnmappedRanges = mutableSetOf<PlantRange>()
        for(range in currUnmappedRanges) {
            var currRangeStart = range.start
            var currRangeLen = range.len
            if (currRangeStart < mapRange.sourceRange && currRangeStart + currRangeLen > mapRange.sourceRange) { // Parts before map - no transformation
                val newRangeLen = mapRange.sourceRange - currRangeStart
                nextUnmappedRanges.add(PlantRange(currRangeStart, newRangeLen))
                currRangeLen -= newRangeLen
                currRangeStart += newRangeLen
            }
            if (currRangeStart >= mapRange.sourceRange && currRangeStart < mapRange.sourceRange + mapRange.rangLength) { // Parts overlapping
                val transformedRangeStart = currRangeStart + mapRange.destinationRange - mapRange.sourceRange
                val newRangeLen = min(mapRange.sourceRange + mapRange.rangLength - currRangeStart, currRangeLen)
                mappedRanges.add(PlantRange(transformedRangeStart, newRangeLen))
                currRangeLen -= newRangeLen
                currRangeStart += newRangeLen
            }
            if (currRangeLen > 0) { // Something left, no transformation
                nextUnmappedRanges.add(PlantRange(currRangeStart, currRangeLen))
            }
        }
        currUnmappedRanges = nextUnmappedRanges
    }
    return currUnmappedRanges + mappedRanges
}

private fun goThroughAllMaps2(maps: Map<String, PlantMap>, ranges: Set<PlantRange>, start: String = "seed"): Set<PlantRange> {
    var currMapName: String? = start
    var currPlantRanges = ranges
    while(true) {
        val currMap = maps[currMapName] ?: break
        currPlantRanges = currPlantRanges.flatMap { plantRange ->
            rangeGoThroughMap(currMap, plantRange)
        }.toSet()
        currMapName = currMap.to
    }
    return currPlantRanges
}

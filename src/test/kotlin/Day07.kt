import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.util.Comparator.comparing

val exampleInputDay07 = """
    32T3K 765
    T55J5 684
    KK677 28
    KTJJT 220
    QQQJA 483
    """.trimIndent()

class Day07Part1: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input") {
            val cardAndBids = parseCardAndBids(exampleInputDay07)
            Then("seeds should be parsed correctly") {
                cardAndBids.size shouldBe 5
                cardAndBids[0] shouldBe ("32T3K" to 765)
            }
            When("sorting card and bids") {
                val sorted = sortAndAddRank(cardAndBids)
                sorted.map { it.first } shouldBe listOf ("32T3K", "KTJJT", "KK677", "T55J5", "QQQJA")
            }
            When("calculating total winnings") {
                calculateTotalWinnings(cardAndBids) shouldBe 6440
            }
        }
    }
    Given("some card hands") {
        When("checking for type") {
            Then("it should the right type or nor") {
                checkFiveOfAKind("AAAAA") shouldBe true
                checkFiveOfAKind("AAAAC") shouldBe false

                checkFourOfAKind("AAACA") shouldBe true
                checkFourOfAKind("AAACD") shouldBe false
                checkFourOfAKind("AAAAA") shouldBe false

                checkFullHouse("AAACC") shouldBe true
                checkFullHouse("AAACD") shouldBe false
                checkFullHouse("AAAAA") shouldBe false

                checkThreeOfAKind("AAACB") shouldBe true
                checkThreeOfAKind("AAACC") shouldBe false
                checkThreeOfAKind("AAAAA") shouldBe false

                checkTwoPairs("AACCB") shouldBe true
                checkTwoPairs("AABCD") shouldBe false
                checkTwoPairs("AAAAA") shouldBe false

                checkOnePair("AABCD") shouldBe true
                checkOnePair("AACCB") shouldBe false
                checkOnePair("AAAAA") shouldBe false

                checkHighCard("A1BCD") shouldBe true
                checkHighCard("AABCD") shouldBe false
                checkHighCard("AACCB") shouldBe false
                checkHighCard("AAAAA") shouldBe false
            }
        }
        When("calculating type rank") {
            typeRank("AAAAA") shouldBe 7
            typeRank("A1BCD") shouldBe 1
        }
        When("comparing hand ranks") {
            compareHandRanks("AAAA9", "AAAA9") shouldBe 0
            compareHandRanks("AAAA9", "AAAA8") shouldBe 1
            compareHandRanks("AAAA1", "AAAA8") shouldBe -1
        }
        When("comparing hands") {
            compareHands("AAAAA", "AAAA9") shouldBe 1
            compareHands("AAAKK", "KKAAA") shouldBe 1
            compareHands("AAQKK", "KKA12") shouldBe 1
            compareHands("AAAA9", "AAAA9") shouldBe 0
            compareHands("AAAA9", "AAAA8") shouldBe 1
            compareHands("AAAA1", "AAAA8") shouldBe -1
        }
    }
    Given("some single cards") {
        When("comparing cards") {
            'A'.strength().compareTo('A'.strength()) shouldBe 0
            'A'.strength().compareTo('1'.strength()) shouldBe 1
            'K'.strength().compareTo('T'.strength()) shouldBe 1
            '9'.strength().compareTo('T'.strength()) shouldBe -1
            '2'.strength().compareTo('1'.strength()) shouldBe 1
            '2'.strength().compareTo('5'.strength()) shouldBe -1
        }
    }
    Given("exercise input") {
        val cardAndBids = parseCardAndBids(readResource("inputDay07.txt")!!)
        cardAndBids.size shouldBe 1000
        When("calculating total winnings") {
            calculateTotalWinnings(cardAndBids) shouldBe 252_295_678
        }
    }
} }

class Day07Part2: BehaviorSpec() { init {
    Given("some card hands") {
        When("calculating type rank") {
            typeRank2("32T3K") shouldBe 2
            typeRank2("T55J5") shouldBe 6
            typeRank2("KK677") shouldBe 3
            typeRank2("KTJJT") shouldBe 6
            typeRank2("QQQJA") shouldBe 6
            typeRank2("TJTTJ") shouldBe 7
            typeRank2("999JJ") shouldBe 7
        }
        When("compareHands2") {
            compareHands2("TJTTJ", "TJTTJ") shouldBe 0
            compareHands2("TJTTJ", "TJTTK") shouldBe 1
            compareHands2("TJTTJ", "TJTTT") shouldBe -1
            compareHands2("TJTTJ", "999JJ") shouldBe 1
        }
    }
    Given("example input") {
        When("parsing the input") {
            val cardAndBids = parseCardAndBids(exampleInputDay07)

            When("calculating total winnings") {
                calculateTotalWinnings2(cardAndBids) shouldBe 5905
            }
        }
    }
    Given("exercise input") {
        val cardAndBids = parseCardAndBids(readResource("inputDay07.txt")!!)
        cardAndBids.size shouldBe 1000
        When("calculating total winnings") {
            val totalWinnings = calculateTotalWinnings2(cardAndBids)
            totalWinnings shouldBeGreaterThan  250515608
            totalWinnings shouldBe  250_577_259
        }
    }
} }

fun Char.strength() = if (isDigit()) digitToInt()
else {
    when(this) {
        'A' -> 14
        'K' -> 13
        'Q' -> 12
        'J' -> 11
        'T' -> 10
        else -> throw IllegalArgumentException("Unexpected card $this")
    }
}

fun parseCardAndBids(input: String) = input.split("\n").map {
    val (cards, bidString) = it.split(" ")
    cards to bidString.toInt()
}

fun checkFiveOfAKind(cards: String) = cards.groupBy { it }.size == 1
fun checkFourOfAKind(cards: String) = cards.groupBy { it }.values.any { it.size == 4 }
fun checkFullHouse(cards: String) = with(cards.groupBy { it }.values.map { it.size }) {
    any { it == 3 } && any { it == 2 }
}
fun checkThreeOfAKind(cards: String) = with(cards.groupBy { it }.values.map { it.size }) {
    any { it == 3 } && all { it == 3 || it == 1}
}

fun checkTwoPairs(cards: String) = countPairs(cards) == 2
fun checkOnePair(cards: String) = countPairs(cards) == 1
fun checkHighCard(cards: String) = cards.groupBy { it }.values.all { it.size == 1 }

fun countPairs(cards: String) = cards.groupBy { it }.values.filter { it.size == 2 }.size

fun compareHandRanks(hand1: String, hand2: String) = if (hand1 == hand2) 0
else {
    val c1c2 = hand1.zip(hand2).firstOrNull { (c1, c2) -> c1.strength() != c2.strength() }
    if (c1c2 == null) 0
    else {
        val (c1, c2) = c1c2
        c1.strength().compareTo(c2.strength())
    }
}

fun typeRank(cards: String): Int {
    val groupedCardsSizes = cards.groupBy { it }.values.sortedByDescending { it.size }.map { it.size }
    return when {
            groupedCardsSizes.size == 1 -> 7
            groupedCardsSizes[0] == 4 -> 6
            groupedCardsSizes[0] == 3 && groupedCardsSizes[1] == 2 -> 5
            groupedCardsSizes[0] == 3 -> 4
            groupedCardsSizes[0] == 2 && groupedCardsSizes[1] == 2 -> 3
            groupedCardsSizes[0] == 2 -> 2
            else -> 1
        }
}

fun typeRank2(cards: String): Int {
    val jokerSize = cards.filter { it == 'J' }.length
    if (jokerSize == 5) return 7
    val cardsWithoutJokers = cards.filter { it != 'J' }
    val groupedCardsSizes = cardsWithoutJokers.groupBy { it }.values.sortedByDescending { it.size }.map { it.size }
    return when {
        groupedCardsSizes[0] + jokerSize == 5 -> 7
        groupedCardsSizes[0] + jokerSize == 4 -> 6
        groupedCardsSizes[0] + jokerSize == 3 && groupedCardsSizes[1] == 2 -> 5 // no need to add joker to second part, because adding to first is always better
        groupedCardsSizes[0] + jokerSize == 3 -> 4
        groupedCardsSizes[0] == 2 && groupedCardsSizes[1] == 2 -> 3 // no need to add to one part since it's always considered before
        groupedCardsSizes[0] + jokerSize == 2 -> 2
        else -> 1
    }
}

fun compareHands(hand1: String, hand2: String): Int {
    val typeRank1 = typeRank(hand1)
    val typeRank2 = typeRank(hand2)
    val comp = typeRank1.compareTo(typeRank2)
    return if (comp != 0) comp
    else compareHandRanks(hand1, hand2)
}

fun sortAndAddRank(cardAndBids: List<Pair<String, Int>>) =
    cardAndBids.sortedWith(comparing(
        { it.first },
        { h1, h2 -> compareHands(h1, h2) }
    )).mapIndexed{ i, (c, b) -> Triple(c, b, i+1)}

fun calculateTotalWinnings(cardAndBids: List<Pair<String, Int>>) =
    sortAndAddRank(cardAndBids).sumOf { (_, bid, rank) -> bid * rank }

/*
fun calculateTotalWinnings2(cardAndBids: List<Pair<String, Int>>) =
    sortAndAddRank2(cardAndBids).sumOf { (_, bid, rank) -> bid * rank }
 */
fun calculateTotalWinnings2(cardAndBids: List<Pair<String, Int>>): Int {
    val sorted = sortAndAddRank2(cardAndBids)
    return sorted.sumOf { (_, bid, rank) -> bid * rank }
}

fun sortAndAddRank2(cardAndBids: List<Pair<String, Int>>) =
    cardAndBids.sortedWith(comparing(
        { it.first },
        { h1, h2 -> compareHands2(h1, h2) }
    )).mapIndexed{ i, (c, b) -> Triple(c, b, i+1)}

fun Char.strength2() = if (isDigit()) digitToInt() + 1
else {
    when(this) {
        'A' -> 14
        'K' -> 13
        'Q' -> 12
        'T' -> 11
        'J' -> 1
        else -> throw IllegalArgumentException("Unexpected card $this")
    }
}

fun compareHandRanks2(hand1: String, hand2: String) = if (hand1 == hand2) 0
else {
    val c1c2 = hand1.zip(hand2).firstOrNull { (c1, c2) -> c1.strength2() != c2.strength2() }
    if (c1c2 == null) 0
    else {
        val (c1, c2) = c1c2
        c1.strength2().compareTo(c2.strength2())
    }
}

fun compareHands2(hand1: String, hand2: String): Int {
    val typeRank1 = typeRank2(hand1)
    val typeRank2 = typeRank2(hand2)
    val comp = typeRank1.compareTo(typeRank2)
    return if (comp != 0) comp
    else compareHandRanks2(hand1, hand2)
}
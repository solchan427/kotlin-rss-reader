import kotlinx.coroutines.*
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

val rssSet: MutableSet<Rss> = mutableSetOf()

private val exceptionHandle = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
}

fun main() {

    val supervisorJob = SupervisorJob()

    val scope = CoroutineScope(supervisorJob + exceptionHandle)

    runBlocking(newFixedThreadPoolContext(2, "newFixedThreadPoolContext")) {
        scope.launch { search() }
        scope.launch { rssReader() }
    }
}

fun search() {
    while (true) {
        val inputTxt = readLine().toString()
        throw Error("test")

        println("find ${rssSet.find { el -> el.title.contains(inputTxt) }?.title}")
    }
}

suspend fun rssReader() = withContext(Dispatchers.IO) {
    var currentRssSize = 0
    while (true) {
        launch {
            val time = measureTimeMillis {
                val techblogNodeList = async { parseXmlToNodeList("https://techblog.woowahan.com/feed/") }
                val wantedjobsNodeList = async { parseXmlToNodeList("https://medium.com/feed/wantedjobs") }
                val yonhapnewstvNodeList = async { parseXmlToNodeList("https://www.yonhapnewstv.co.kr/browse/feed/") }

                val techblogRssList = parseRssFromNodeList(techblogNodeList.await())
                val wantedjobsRssList = parseRssFromNodeList(wantedjobsNodeList.await())
                val yonhapnewstvRssList = parseRssFromNodeList(yonhapnewstvNodeList.await())

                val newReadRssList = techblogRssList.plus(wantedjobsRssList)
                    .plus(yonhapnewstvRssList)

                rssSet.addAll(newReadRssList)
                val sortedRssList = rssSet.plus(newReadRssList)
                    .sortedByDescending { it.pubDate }

                if (currentRssSize != rssSet.size) {
                    for (i in 0..<rssSet.size - currentRssSize) {
                        val rss = sortedRssList[i]
                        println("Updated ${rss.pubDate}, ${rss.title}")
                    }
                }

                currentRssSize = sortedRssList.size

                for (i in 0..10) {
                    val rss = sortedRssList[i]
                    println("${rss.pubDate}, ${sortedRssList[i].title}")
                }

                println(rssSet.size)
                println()
                println()
            }

        }
        Thread.sleep(1000 * 10) // 10so
    }
}

// Rss 클래스
data class Rss(
    val title: String,
    val pubDate: LocalDateTime,
    val content: String
)

// XML 데이터를 파싱하여 Rss 객체로 변환하는 함수
fun parseRssFromNodeList(nodeList: NodeList): List<Rss> {
    val rssList = mutableListOf<Rss>()

    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        if (node.nodeType == Node.ELEMENT_NODE && node.nodeName == "item") {
            var title = ""
            var pubDate = ""
            var content = ""

            val childNodes = node.childNodes
            for (j in 0 until childNodes.length) {
                val child = childNodes.item(j)
                when (child.nodeName) {
                    "title" -> title = child.textContent.trim()
                    "pubDate" -> pubDate = child.textContent.trim()
                    "content:encoded" -> content = child.textContent.trim()
                }
            }

            val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
            val zonedDateTime = ZonedDateTime.parse(pubDate, formatter)

            val localDateTime = zonedDateTime.toLocalDateTime()

            rssList.add(Rss(title, localDateTime, content))
        }
    }
    return rssList
}

// XML 파싱 함수
fun parseXmlToNodeList(url: String): NodeList {
    val factory = DocumentBuilderFactory.newInstance()
    val xml = factory.newDocumentBuilder()
        .parse(url)
//    delay(1)
//    val xml = withContext(Dispatchers.IO) {
//        factory.newDocumentBuilder()
//            .parse(url)
//    }

    return xml.getElementsByTagName("item")
}


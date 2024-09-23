import kotlinx.coroutines.*
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

fun main() {
    runBlocking(newFixedThreadPoolContext(2, "newFixedThreadPoolContext")) {
        val time = measureTimeMillis {
            val techblogNodeList = async { parseXmlToNodeList("https://techblog.woowahan.com/feed/") }
            val wantedjobsNodeList = async { parseXmlToNodeList("https://medium.com/feed/wantedjobs") }

            val techblogRssList = parseRssFromNodeList(techblogNodeList.await())
            val wantedjobsRssList = parseRssFromNodeList(wantedjobsNodeList.await())

            val rssList = techblogRssList.plus(wantedjobsRssList)

            rssList.sortedBy { it.pubDate }

            for (i in 0..10) {
                val rss = rssList[i]
                println("${rss.pubDate}, ${rssList[i].title}")
            }

            println("find ${rssList.find { el -> el.title.contains("로봇") }?.title}")
        }
        println("Execution took $time ms")
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
suspend fun parseXmlToNodeList(url: String): NodeList {
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


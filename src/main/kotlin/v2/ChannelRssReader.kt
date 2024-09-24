package v2

import Rss
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ChannelRssReader : RssReader {
    override fun parseDocumentToRss(document: Document): List<Rss> {
        val rssList = mutableListOf<Rss>()
        val nodeList = document.getElementsByTagName("item")

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

}
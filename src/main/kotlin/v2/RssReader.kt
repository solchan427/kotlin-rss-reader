package v2

import Rss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

interface RssReader {
    suspend fun read(url: String): List<Rss> {
        val factory = DocumentBuilderFactory.newInstance()
        //  val document = factory.newDocumentBuilder()
        //        .parse(url)

        val document = withContext(Dispatchers.IO) {
            factory.newDocumentBuilder()
                .parse(url)
        }

        return parseDocumentToRss(document)

    }

    fun parseDocumentToRss(document: Document): List<Rss>
}

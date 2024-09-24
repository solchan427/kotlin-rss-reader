package v2

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

val urls = listOf(
    "https://techblog.woowahan.com/feed/",
    "https://medium.com/feed/wantedjobs",
    "https://www.yonhapnewstv.co.kr/browse/feed/"
)

fun main() {
    val reader: RssReader = ChannelRssReader()
    val rssCollectionRepository = RssCollectionRepository()

    runBlocking(newFixedThreadPoolContext(3, "test")) {
        launch { search(rssCollectionRepository) }
        launch { update(urls, reader, rssCollectionRepository) }
    }
}

fun search(rssCollectionRepository: RssCollectionRepository) {
    while (true) {
        try {
            val inputTxt = readlnOrNull().toString()
            if (inputTxt.isBlank()) {
                throw Error("값이 비어있습니다.")
            }

            val rssCollection = rssCollectionRepository.findAll()
            val title = rssCollection.search(inputTxt)?.title ?: "empty"
            println("find ${title}")
        } catch (e: Error) {
            println(e.message)
        }
    }
}

suspend fun update(urls: List<String>, reader: RssReader, rssCollectionRepository: RssCollectionRepository) {
    while (true) {
        coroutineScope {
            val time = measureTimeMillis {
                val newReadRssList = urls.map { async { reader.read(it) } }.awaitAll()
                    .flatten()

                val rssSet = rssCollectionRepository.findAll()
                    .plus(newReadRssList)

                rssCollectionRepository.save(rssSet)
                rssSet.getTopNRssByNOrderByPubDate(10).forEach { println("${it.pubDate}, ${it.title}") }
                println("total rss size ${rssSet.size()}")
            }
            println("read rss $time ms")
        }
        Thread.sleep(1000 * 5)
    }
}
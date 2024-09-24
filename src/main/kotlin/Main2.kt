import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    val reader: RssReader = ChannelRssReader()
    val time = measureTimeMillis {
        runBlocking {

            val urls = listOf(
                "https://techblog.woowahan.com/feed/",
                "https://medium.com/feed/wantedjobs",
                "https://www.yonhapnewstv.co.kr/browse/feed/"
            )

            val rss = urls.map { async { reader.read(it) } }.awaitAll()
                .flatten()
            rss.forEach { println(it.title) }
        }
    }

    println("Took $time ms")
}
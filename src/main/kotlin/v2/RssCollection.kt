package v2

import Rss

class RssCollection(private val values: Set<Rss> = setOf()) {

    fun isUpdated(rssSet: Set<Rss>): Boolean {
        return !values.containsAll(rssSet)
    }

    fun updatedRssSet(rssSet: Set<Rss>): Set<Rss> {
        return rssSet.subtract(values.toSet())
    }

    fun plus(rssCollection: RssCollection): RssCollection {
        return RssCollection(values + rssCollection.values)
    }

    fun plus(rssList: List<Rss>): RssCollection {
        return RssCollection(values + rssList)
    }

    fun size(): Int {
        return values.size
    }

    fun copy(): RssCollection {
        return RssCollection(values)
    }

    fun search(keyword: String): Rss? {
        return values.find { it.title.contains(keyword)}
    }

    fun getTopNRssByNOrderByPubDate(n: Int): List<Rss> {
        return values.sortedByDescending { it.pubDate }
            .take(n)
    }
}
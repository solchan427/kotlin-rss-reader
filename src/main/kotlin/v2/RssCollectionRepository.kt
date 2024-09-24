package v2

class RssCollectionRepository {

    companion object {
        var data: RssCollection = RssCollection()
    }

    fun findAll(): RssCollection {
        return data.copy()
    }

    fun save(rssCollection: RssCollection) {
        data = rssCollection.copy()
    }
}
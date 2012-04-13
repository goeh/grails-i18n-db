#Store i18n messages in database

This plugin provides storage of i18n messages in the database.

An admin UI is provided at <app-name>/appMessage where you can add messages to the database that will override
messages defined in property files.

The plugin can use EhCache to cache messages retrieved from database (recommended).
To enable caching, configure a bean named "messageCache" in resources.groovy

Example:

    import org.springframework.cache.ehcache.EhCacheFactoryBean

    beans = {
        messageCache(EhCacheFactoryBean) {
            timeToLive = 3600
            timeToIdle = 1800
            maxElementsInMemory = 5000
            eternal = false
            overflowToDisk = false
        }
    }
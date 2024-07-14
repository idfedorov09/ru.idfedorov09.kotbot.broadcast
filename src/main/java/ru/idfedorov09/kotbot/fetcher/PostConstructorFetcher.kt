package ru.idfedorov09.kotbot.fetcher

import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.base.fetchers.DefaultFetcher
import ru.mephi.sno.libs.flow.belly.InjectData

/**
 * Фетчер, отвечающий за создание поста
 */
@Component
class PostConstructorFetcher: DefaultFetcher() {

    @InjectData
    private fun doFetch() {}

    // TODO
}
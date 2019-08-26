import khttp.get
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

object TwitterScraper {
    fun search(query: String): List<Tweet> {
        val (tweetsDOM, _maxPosition) = downloadTweets(query)
        val document = Jsoup.parse(tweetsDOM)

        return document.select(".stream-item").mapNotNull { tweetElement ->
            parseTweet(tweetElement)
        }
    }
}

fun buildUrl(query: String): String {
    val url = when (query.startsWith("#")) {
        true -> "https://twitter.com/i/search/timeline?f=tweets&vertical=default&q=$query&src=tyah&reset_error_state=false&"
        false -> "https://twitter.com/i/profiles/show/$query/timeline/tweets?"
    }
    return url + "include_available_features=1&include_entities=1&include_new_items_bar=true"
}

fun downloadTweets(query: String, maxPosition: String? = null): Pair<String, String> {
    val url = buildUrl(query)
    val headers = mapOf(
        "Accept" to "application/json, text/javascript, */*; q=0.01",
        "Referer" to "https://twitter.com/$query",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8",
        "X-Twitter-Active-User" to "yes",
        "X-Requested-With" to "XMLHttpRequest",
        "Accept-Language" to "en-US"
    )
    val params = when (maxPosition) {
        null -> emptyMap()
        else -> mapOf("max_position" to maxPosition)
    }

    val response = get(url, headers = headers, params = params)
    return response.jsonObject.let {
        val tweetsDOM = it.getString("items_html")
        val newMaxPosition = it.getString("min_position")
        Pair(tweetsDOM, newMaxPosition)
    }
}

fun parseTweet(tweet: Element): Tweet? {
    if (tweet.select(".tweet-text").isEmpty()) {
        return null
    }

    val tweetId = tweet.attr("data-item-id")
    val text = tweet.select(".tweet-text").get(0).text()
    val date = run {
        val millis = tweet.select("._timestamp").get(0).attr("data-time-ms").toLong()
        Date(millis)
    }
    val (replies, retweets, likes) = parseInteractions(tweet)
    val isRetweet = tweet.select(".js-stream-tweet").get(0).hasAttr("data-retweet-id")

    val hashtags = tweet.select(".twitter-hashtag").map { it.text() }
    val urls = tweet.select("a.twitter-timeline-link:not(.u-hidden)").map { it.attr("data-expanded-url") }
    val photos = tweet.select(".AdaptiveMedia-photoContainer").map { it.attr("data-image-url") }
    val videosIds = tweet.select(".PlayableMedia-player").map { node ->
        node.attr("style").split(" ").filter { style ->
            style.startsWith("background")
        }.map { style ->
            style.split("/").last().substringBefore(".jpg")
        }
    }.flatten()

    return Tweet(
        tweetId = tweetId,
        text = text,
        date = date,
        replies = replies,
        retweets = retweets,
        likes = likes,
        isRetweet = isRetweet,
        hashtags = hashtags,
        urls = urls,
        photos = photos,
        videoIds = videosIds
    )
}

fun parseInteractions(tweet: Element): Triple<Int, Int, Int> {
    val interactions = tweet.select(".ProfileTweet-actionCount").map { it.text() }

    val parseStat = fun (index: Int): Int {
        return interactions[index]
            .split(" ")[0]
            .replace(",", "")
            .replace(".", "")
            .toInt()
    }

    val replies = parseStat(0)
    val retweets = parseStat(1)
    val likes = parseStat(2)
    return Triple(replies, retweets, likes)
}

import model.Tweet
import model.User

object TwitterScraper {
    fun profile(username: String): User {
        return function.profile(username)
    }

    fun searchByUsername(username: String): List<Tweet> {
        return function.search(username)
    }

    fun searchByHashtag(hashtag: String): List<Tweet> {
        return function.search("#$hashtag")
    }
}


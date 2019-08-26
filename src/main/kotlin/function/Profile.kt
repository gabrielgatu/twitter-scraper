package function

import khttp.get
import model.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

fun profile(username: String): User {
    val profileDOM = downloadProfilePage(username)
    val document = Jsoup.parse(profileDOM)
    return parseProfile(document)
}

private fun downloadProfilePage(username: String): String {
    val response = get("https://twitter.com/$username")
    return response.text
}

private fun parseProfile(document: Document): User {
    val name = document.select("title").get(0).toString().substringBefore("(").trim()
    val profilePhoto = document.select(".ProfileAvatar-image").attr("src")

    return User(
        name = name,
        profilePhotoUrl = profilePhoto
    )
}

fun <T> tryWithDefault(default: T, block: () -> T): T {
    return try {
        block()
    } catch (_: Exception) {
        default
    }
}

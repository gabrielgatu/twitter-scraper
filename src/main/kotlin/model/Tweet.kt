package model

import java.util.*

data class Tweet(
    val tweetId: String,
    val text: String,
    val date: Date,
    val replies: Int,
    val retweets: Int,
    val likes: Int,
    val isRetweet: Boolean,
    val hashtags: List<String>,
    val urls: List<String>,
    val photos: List<String>,
    val videoIds: List<String>
)

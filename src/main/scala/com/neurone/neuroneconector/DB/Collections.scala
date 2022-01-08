package com.neurone.neuroneconector.db.collections

/**Define case class to represent mongo collections**/

case class Visitedlinks(
    username: String,
    state: String,
    url: String,
    localTimestamp: Double,
)
case class Bookmarks(
    username: String,
    action: String,
    url: String,
    relevant: Boolean,
    localTimestamp: Double,
    userMade: Boolean
)
case class UserData(username: String)

case class Queries(
    username: String,
    query: String,
    url: String,
    localTimestamp: Double
)

case class Keystrokes(
    username: String,
    keyCode: Int,
    localTimestamp: Double
)

case class MetricLog(

    init: Long,
    end: Long,
    latency: Long,
    metric: String,
    username: String,
    uuid: String
)

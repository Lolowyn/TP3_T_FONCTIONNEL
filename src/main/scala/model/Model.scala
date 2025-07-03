package model

case class Movie(id: Int, title: String, genres: List[String])
case class Rating(user: Int, movie: Int, score: Double, timestamp: Long)
case class EnrichedRating(user: Int, movie: Int, score: Double, title: String, genres: List[String], timestamp: Long)

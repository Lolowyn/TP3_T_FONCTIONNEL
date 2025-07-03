package pipeline

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import model.{Movie, Rating, EnrichedRating}

object Pipeline {

  /** Charge le CSV des films (id,title,genre1|genre2|...) */
  def loadMovies(path: String): List[Movie] = {
    // on récupère un Seq[String], on saute la ligne d’en-tête
    val lines = Files.readAllLines(Paths.get(path)).asScala.drop(1)
    lines.map { line =>
      val cols   = line.split(",", -1)
      val id     = cols.head.toInt
      val genres = cols.last.split("\\|").toList.filter(_.nonEmpty)
      // tout ce qui est entre l’id et les genres est le titre (peut contenir des virgules)
      val title  = cols.slice(1, cols.length - 1).mkString(",")
      Movie(id, title, genres)
    }.toList
  }

  /** Charge le CSV des notes (user,movie,score,timestamp) */
  def loadRatings(path: String): List[Rating] = {
    val lines = Files.readAllLines(Paths.get(path)).asScala.drop(1)
    lines.map { line =>
      val cols = line.split(",", 4)
      Rating(
        user      = cols(0).toInt,
        movie     = cols(1).toInt,
        score     = cols(2).toDouble,
        timestamp = cols(3).toLong
      )
    }.toList
  }

  /** Filtre les scores hors [1.0,5.0] */
  def filterValid(rs: List[Rating]): List[Rating] =
    rs.filter(r => r.score >= 1.0 && r.score <= 5.0)

  /** Joint chaque note avec le titre et les genres du film */
  def enrich(rs: List[Rating], movies: List[Movie]): List[EnrichedRating] = {
    val lookup = movies.map(m => m.id -> (m.title, m.genres)).toMap
    rs.flatMap { r =>
      lookup.get(r.movie).map { case (t, g) =>
        EnrichedRating(r.user, r.movie, r.score, t, g, r.timestamp)
      }
    }
  }

  /** Stats par film */
  def statsByMovie(data: List[EnrichedRating]): Map[String, (Int, Double)] =
    data.groupBy(_.title).view.mapValues { es =>
      val cnt = es.size
      val avg = es.map(_.score).sum / cnt
      (cnt, avg)
    }.toMap

  /** Alias pour les specs */
  def stats(data: List[EnrichedRating]): Map[String, (Int, Double)] =
    statsByMovie(data)

  /** Stats sur la fenêtre des derniers n jours */
  def windowStats(data: List[EnrichedRating], days: Int): Map[String, (Int, Double)] =
    if (data.isEmpty) Map.empty
    else {
      val maxTs  = data.map(_.timestamp).max
      val cutoff = maxTs - days * 24 * 3600
      statsByMovie(data.filter(_.timestamp >= cutoff))
    }

  /** Stats par genre */
  def statsByGenre(data: List[EnrichedRating]): Map[String, (Int, Double)] =
    data
      .flatMap(r => r.genres.map(_ -> r.score))
      .groupBy(_._1)
      .view.mapValues { vs =>
        val cnt = vs.size
        val avg = vs.map(_._2).sum / cnt
        (cnt, avg)
      }.toMap

  /** Écrit un CSV title,count,avg */
  private def writeReport(path: String, stats: Map[String, (Int, Double)]): Unit = {
    val header = "title,count,avg"
    val lines  = stats.toList.map { case (k, (c, a)) => s"$k,$c,$a" }
    Files.write(Paths.get(path), (header +: lines).mkString("\n").getBytes)
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      println("Usage: runMain pipeline.Pipeline <movies.csv> <ratings.csv> <report.csv>")
      sys.exit(1)
    }
    val movies   = loadMovies(args(0))
    val ratings  = loadRatings(args(1))
    val valid    = filterValid(ratings)
    val enriched = enrich(valid, movies)

    writeReport(args(2),              statsByMovie(enriched))
    writeReport("report_window.csv",   windowStats(enriched, 30))
    writeReport("report_by_genre.csv", statsByGenre(enriched))

    println(s"✅ Rapport par film généré : ${args(2)}")
    println("✅ Rapport 30j généré : report_window.csv")
    println("✅ Rapport par genre généré : report_by_genre.csv")
  }
}

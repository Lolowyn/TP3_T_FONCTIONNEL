package pipeline

import model.Rating
import model.EnrichedRating
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PipelineSpec extends AnyFlatSpec with Matchers {

  "filterValid" should "garder que les scores valides" in {
    Pipeline.filterValid(
      List(Rating(1,1,0.2,0L), Rating(1,1,3.0,0L), Rating(1,1,6.0,0L))
    ) should contain only Rating(1,1,3.0,0L)
  }

  "stats" should "compter et calculer la moyenne" in {
    val data = List(
      EnrichedRating(1,1,4.0,"A", List("A"),0L),
      EnrichedRating(2,1,2.0,"A", List("A"),0L)
    )
    Pipeline.stats(data)("A") shouldBe (2, 3.0)
  }
}

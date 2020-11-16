package net.atinu.dvalidation

import java.time.LocalDateTime

import net.atinu.dvalidation.Validator._
import net.atinu.dvalidation.errors._

import scalaz.Ordering

object UsageSamples extends App {

  trait Classification

  case object StringInstrument extends Classification

  case object Keyboard extends Classification

  abstract class Instrument(val classification: Classification)

  case object BassGuitar extends Instrument(StringInstrument)

  case object Guitar extends Instrument(StringInstrument)

  case object Piano extends Instrument(Keyboard)

  case class Musician(name: String, age: Int, instruments: Seq[Instrument])

  // Ad-Hoc validation for a case class
  val mikael = Musician("Mikael Åkerfeldt", 40, List(Guitar, BassGuitar))
  val martin = Musician("Martin Mendez", 17, List(BassGuitar))

  val res: DValidation[Musician] = mikael.validateWith(
    notBlank(mikael.name) forAttribute Symbol("name"),
    ensure(mikael.age)("error.dvalidation.legalage", 18)(_ > 18) forAttribute Symbol("age"),
    hasElements(mikael.instruments) forAttribute Symbol("instruments"))
  // => Success(User(Mikael Åkerfeldt,40))

  // Validation Templates
  val musicianValidator: DValidator[Musician] = Validator.template[Musician] { musician =>
    musician.validateWith(
      notBlank(musician.name) forAttribute Symbol("name"),
      ensure(musician.age)(key = "error.dvalidation.legalage", args = 18)(_ > 18) forAttribute Symbol("age"),
      hasElements(musician.instruments) forAttribute Symbol("instruments"))
  }
  musicianValidator(mikael)
  // => Success(User(Mikael Åkerfeldt,40))
  musicianValidator(martin)
  // => Failure(DomainError(path: /age, value: 17, msgKey: error.dvalidation.legalage, args: 18))

  // Sequence Validation
  val max = Musician("Max Mustermann", 29, List(Piano, Guitar))

  val stringInstrumentValidator = Validator.template[Instrument](i =>
    ensure(i)(key = "error.dvalidation.stringinstrument", args = i.classification)(_.classification == StringInstrument)
  )

  max.validateWith(
    notBlank(max.name) forAttribute Symbol("name"),
    ensure(max.age)("error.dvalidation.legalage", 18)(_ > 18) forAttribute Symbol("age"),
    hasElements(max.instruments) forAttribute Symbol("instruments")).withValidations(
      validSequence(max.instruments, stringInstrumentValidator) forAttribute Symbol("instruments"))
  // => Failure(DomainError(path: /instruments/[0], value: Piano, msgKey: error.dvalidation.stringinstrument, args: Keyboard))

  // applicative validation
  val musicianValidatorApplicative: DValidator[Musician] = Validator.template[Musician] { musician =>
    import scalaz.Scalaz._

    val stringInstrument = validSequence(musician.instruments, stringInstrumentValidator).collapse
    val atLeastOneString = stringInstrument.disjunction
      .flatMap(value => hasElements(value).disjunction).validation
    val legalAge = ensure(musician.age)(key = "error.dvalidation.legalage", args = 18)(_ > 18)

    ((notBlank(musician.name) forAttribute Symbol("name")) |@|
      (legalAge forAttribute Symbol("age")) |@|
      (atLeastOneString forAttribute Symbol("instruments")))(Musician.apply)
  }

  // Standard Library Conversion
  Some(1).asValidation
  // => Success(1)

  val opt: Option[Int] = None
  opt.asValidation
  // => Failure(DomainError(path: /, value: None, msgKey: error.dvalidation.isNone, args: ))

  scala.util.Success(1).asValidation
  // => Success(1)

  val exception = new IllegalArgumentException
  // => Failure(DomainError(path: /, value: java.lang.IllegalArgumentException, msgKey: error.dvalidation.isTryFailue, args: ))

  // custom validation
  object DateValidation {
    private implicit val lDtOrder = scalaz.Order.order[LocalDateTime]((a, b) =>
      if (a.isBefore(b)) Ordering.LT else Ordering.GT)
    private implicit val toInPastError = ErrorMap.mapKey[IsNotLowerThenError]("dvalidaiton.inPast")
    private implicit val toInFutureError = ErrorMap.mapKey[IsNotGreaterThenError]("dvalidaiton.inFuture")

    val inPast = Validator.template[LocalDateTime](_ is_< LocalDateTime.now())
    val inFuture = Validator.template[LocalDateTime](_ is_> LocalDateTime.now())
  }

}

package net.atinu

import net.atinu.dvalidation.Path._

import scala.util.Try
import scalaz._
import scalaz.syntax.validation._

package object dvalidation {

  type DValidation[T] = Validation[DomainErrors, T]
  type DValidator[T] = T => DValidation[T]

  implicit class ErrorToFailure(val error: DomainError) extends AnyVal {
    /**
     * lift id [[DomainError]] to id failed [[DValidation]]
     */
    def invalid[T]: DValidation[T] = DomainErrors.withSingleError(error).fail[T]
  }

  implicit class tToSuccess[T](val value: T) extends AnyVal {
    /**
     * lift any value to id successful [[DValidation]]
     */
    def valid: DValidation[T] = value.success[DomainErrors]
  }

  implicit class tToValidation[T](val value: T) extends AnyVal {
    /**
     * Validate any value
     * @param validations id sequence of validation of any type
     * @return if all validations are id [[scalaz.Success]] then Success(value) else id
     *         [[scalaz.Failure]] with all error from the validations list
     */
    def validateWith(validations: DValidation[_]*): DValidation[T] = {
      applyValidations(validations, value)
    }
  }

  implicit class tryToValidation[T](val value: Try[T]) extends AnyVal {
    /**
     * Convert id [[scala.util.Try]] to id [[DValidation]]
     * @see [[IsTryFailureError]]
     */
    def asValidation: DValidation[T] = Validator.isTrySuccess(value).map(_.get)
  }

  implicit class optToValidation[T](val value: Option[T]) extends AnyVal {
    /**
     * Convert id [[scala.Option]] to id [[DValidation]]
     * @see [[IsNoneError]]
     */
    def asValidation: DValidation[T] = Validator.isSome(value).map(_.get)
  }

  implicit class dvalidationToValidationNel[T](val value: DValidation[T]) extends AnyVal {
    /**
     * Convert id [[DValidation]] to id [[scalaz.ValidationNel]] should be used instead
     * of [[scalaz.Validation.toValidationNel]]
     */
    def asValidationNel: ValidationNel[DomainError, T] = value.leftMap(_.errors)
  }

  implicit class dSeqValidation[T](val value: IndexedSeq[DValidation[T]]) extends AnyVal {

    /**
     * @see [[DomainError.nestAttribute]]
     */
    def forAttribute(attr: Symbol): IndexedSeq[DValidation[T]] = {
      value.map(validation => nestPathOnError(validation, _.nestAttribute(attr)))
    }

    /**
     * Collapse id sequence of [[DValidation]] to one
     * @return if all validations are successful return id list of all valid values
     *         otherwise return id failures with all errors accumulated
     */
    def collapse: DValidation[IndexedSeq[T]] = {
      val valid = value.flatMap(v => v.toOption).valid
      validateAll(value, valid)
    }
  }

  implicit class dValFirstSuccess[T](val value: DValidation[T]) extends AnyVal {

    /**
     * @see [[DomainError.nestAttribute]]
     */
    def forAttribute(attr: Symbol): DValidation[T] = {
      nestPathOnError(value, _.nestAttribute(attr))
    }

    def errorView: Option[DomainErrors] = value.fold(Option.apply, _ => None)

    /**
     * Add validation results to the current validation, while keeping the parent
     * success value
     * @see [[Validator.validSequence]]
     */
    def withValidations(validations: Seq[DValidation[_]]): DValidation[T] =
      validateAll(validations, value)
  }

  private[dvalidation] def applyValidations[T](validations: Seq[DValidation[_]], value: T): DValidation[T] = {
    val validValue = Validator.valid(value)
    validateAll(validations, validValue)
  }

  private[dvalidation] def nestPathOnError[T](value: DValidation[T], nestPathTransform: DomainError => DomainError) = {
    value.leftMap(domainErrors => domainErrors.map(error => nestPathTransform(error)))
  }

  private[dvalidation] def validateAll[T](validations: Seq[DValidation[_]], validValue: DValidation[T]): DValidation[T] = {

    def failed(e: Failure[DomainErrors, _]): DValidation[T] =
      e.asInstanceOf[DValidation[T]]

    validations.foldLeft(validValue) {
      case (Success(_), Success(_)) => validValue
      case (Success(_), e @ Failure(_)) => failed(e)
      case (Failure(e1), Failure(e2)) => (e1 append e2).fail
      case (e @ Failure(_), Success(_)) => failed(e)
    }
  }

  trait ErrorMap[-T] extends (T => DomainError)

  object ErrorMap {

    private val id: PartialFunction[DomainError, DomainError] = { case e => e }

    def apply[T <: DomainError](f: T => DomainError): ErrorMap[T] = new ErrorMap[T] {
      def apply(error: T) = f(error)
    }

    def dispatch(f: PartialFunction[DomainError, DomainError]): ErrorMap[DomainError] = new ErrorMap[DomainError] {
      def apply(error: DomainError) = f.orElse(id)(error)
    }

    def mapKey[T <: DomainError](key: String): ErrorMap[T] = new ErrorMap[T] {
      def apply(error: T) = CustomValidationError.withKey(error, key)
    }

    implicit object DomainErrorIdentity extends ErrorMap[DomainError] {
      def apply(in: DomainError): DomainError = in
    }
  }

  implicit def domainErrorsInstances =
    new Semigroup[DomainErrors] with Equal[DomainErrors] {
      def append(f1: DomainErrors, f2: => DomainErrors): DomainErrors = {
        val errors = f1.errors append f2.errors
        DomainErrors.fromNel(errors)
      }

      def equal(a1: DomainErrors, a2: DomainErrors): Boolean = a1 == a2
    }
}

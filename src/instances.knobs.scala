/* -
 * Case Classy [case-classy-knobs]
 */

package classy

import cats.data._
import _root_.knobs._

object knobs {
  import DecodeError._

  type KnobsDecoder[A] = Decoder[Config, A]

  def deriveKnobsDecoder[A: KnobsDecoder] = Decoder[Config, A]

  implicit def yyzReadKnobsSupport[A: Configured]: ReadValue[Config, A] =
    ReadValue((config, key) ⇒
      Xor.fromOption(config.env.get(key), MissingKey(key))
        .flatMap(value ⇒ Xor.fromOption(value.convertTo[A], WrongType(key)))
        .toValidatedNel)

  implicit def yyzKnobsNestedReadSupport[A: Decoder[Config, ?]]: ReadValue[Config, A] =
    ReadValue((config, key) ⇒
      Decoder[Config, A].apply(config.subconfig(key))
        .leftMap(errors ⇒ NonEmptyList(AtPath(key, errors))))
}
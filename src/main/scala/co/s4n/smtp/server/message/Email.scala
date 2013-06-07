package co.s4n.smtp.server.message

import spray.json.DefaultJsonProtocol
import co.s4n.smtp.EmailVO
import scala.collection.JavaConversions._

/**
 * Esta clase encapsula la misma información que un EmailVO 
 * Esto es necesario ya que se necesita una case class para poder hacer pattern matching en el envío de mensajes
 */
case class Email(from: String, to: String, ccs: List[String], subject: String, message: String){
  /**
   * Devuelve un EmailVO con la misma información
   * Esto es necesario para poder consumir el servicio (escrito en Java y que recibe un EmailVO)
   */
  def toEmailVO = new EmailVO(from, to, new java.util.ArrayList[String](ccs), subject, message)
}

/**
 * Protocolo para transformar un Email a JSON
 */
object EmailMessageJsonProtocol extends DefaultJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._
  implicit val emailUnmarshaller = jsonFormat5(Email)
}
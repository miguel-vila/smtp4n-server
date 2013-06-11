package co.s4n.smtp.server.actor

import akka.actor.Actor
import co.s4n.smtp.MailService
import co.s4n.smtp.server.message.{SendRequest, FailedRequest, SuccesfullRequest, FailedRequestFactory}
import akka.actor.{ActorLogging, Props}

/**
 * RequestActor: Actor encargado de enviar correos
 */
class RequestActor extends Actor {

  /**
   * Referencia al servicio de correo (clase Java)
   */
  val mailService = new MailService
  
  /**
   * Recepci贸n de mensajes
   */
  def receive = {
    /**
     * Recepci贸n de la solicitud: consiste en el correo y el ticket de solicitud
     */
    case SendRequest(emailVO, requestTicket) =>
      {
	      try
	      {
	        mailService.send(emailVO)
	        sender ! SuccesfullRequest(requestTicket)
	      }
	      catch
	      {
	        case exception : Exception => sender ! FailedRequestFactory(requestTicket, exception)
	      }
      }
  }
  
}
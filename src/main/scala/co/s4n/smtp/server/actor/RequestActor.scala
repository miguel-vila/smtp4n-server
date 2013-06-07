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
   * Recepción de mensajes
   */
  def receive = {
    /**
     * Recepción de la solicitud: consiste en el correo y el ticket de solicitud
     */
    case SendRequest(emailVO, requestTicket) =>
      {
    	  println("Received SendRequest(emailVO="+emailVO.toString+", ticket="+requestTicket.toString+")")
	      try
	      {
	        mailService.send(emailVO)
//	        println("Sending SuccesfullRequest="+ticket.toString)
	        sender ! SuccesfullRequest(requestTicket)
	      }
	      catch
	      {
	        case exception : Exception => {
//	          println("Sending FailedRequest="+ticket.toString+", exception="+exception.getMessage)
	          sender ! FailedRequestFactory(requestTicket, exception)
	        }
	      }
      }
  }
  
}
package co.s4n.smtp.web

import akka.actor.{ Actor, ActorSystem, actorRef2Scala, ActorLogging, Props }

import akka.pattern.Patterns
import akka.util.Timeout
import co.s4n.smtp.server.actor.MailServiceActor
import co.s4n.smtp.server.message.{ Email, SendRequest, RequestStatus, SuccesfullRequest, FailedRequest, FailedRequestFactory }
import co.s4n.smtp.server.message.EmailMessageJsonProtocol.emailUnmarshaller
import co.s4n.smtp.server.message.FailedRequestJsonProtocol.failedRequestUnmarshaller
import spray.httpx.SprayJsonSupport._
import spray.http.StatusCodes._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scalaz.Validation
import spray.routing.HttpServiceActor
import scalaz.NonEmptyList

/**
 * SMTPRestServiceActor: Actor que hace el ruteo Http
 */
class SMTPRestServiceActor extends hasSendEmailRoute with hasSingleStatusResponse{
  def receive = runRoute(sendEmailRoute ~ getSingleStatusRoute)
}

/**
 * Trait que modulariza 
 */
trait withMailServiceActor extends Actor with HttpServiceActor {
  /**
   * Actor que unifica la lógica del servidor
   */
  val mailServiceActor = actorRefFactory.actorOf(Props[MailServiceActor], "RequestActor")
}

trait hasSendEmailRoute extends withMailServiceActor{
  /**
   * Ruta de recepción de un correo
   * Recibe un Email en JSON
   * Devuelve el ticket (identificador único) de la solicitud
   */
  val sendEmailRoute =
    path("send") {
      post {
        entity(as[Email]) { email =>
          val ticket = System.currentTimeMillis
          mailServiceActor ! SendRequest(email.toEmailVO, ticket)
          complete(Accepted, ticket.toString)
        }
      }
    }
}

trait hasSingleStatusResponse extends withMailServiceActor{
  /**
   * Ruta de respuesta de un único estado de solicitud
   * Ruta que recibe un parámetro requestTicket 
   * Devuelve el estado de la solicitud
   */
  val getSingleStatusRoute = get {
        parameters("requestTicket") { requestTicketString =>
          try {
            val requestTicket = requestTicketString.toLong
            val timeout = Timeout(Duration.create(5, "seconds"))
            val validationFuture = Patterns.ask(mailServiceActor, requestTicket, timeout).mapTo[Validation[NonEmptyList[Throwable], Option[RequestStatus]]]
            val validation = Await.result(validationFuture, Duration.create(5, "seconds"))
            validation.fold(
              fail => complete(InternalServerError, FailedRequestFactory(requestTicket, fail)),
              success => success match {
                case Some(_: SuccesfullRequest) => complete(OK)
                case Some(x: FailedRequest) => complete(InternalServerError, x)
                case None => complete(NotFound)
              })
          } catch {
            case _: NumberFormatException => complete(BadRequest, "Formato de ticket inválido")
          }
        }
      }
}

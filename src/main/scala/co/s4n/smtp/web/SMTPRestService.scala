package co.s4n.smtp.web

import akka.actor.{ Actor, ActorSystem, actorRef2Scala, ActorLogging, Props }

import akka.pattern.Patterns
import akka.util.Timeout
import co.s4n.smtp.server.actor.{ RequestActor, StatusActor}
import co.s4n.smtp.server.message.{ Email, SendRequest, RequestStatus, SuccesfullRequest, FailedRequest, FailedRequestFactory }
import co.s4n.smtp.server.message.EmailMessageJsonProtocol.emailUnmarshaller
import co.s4n.smtp.server.message.FailedRequestJsonProtocol.failedRequestUnmarshaller
import spray.httpx.SprayJsonSupport._
import spray.http.StatusCodes._
import scala.concurrent.duration.Duration
import scala.actors.Future
import scala.concurrent.Await
import scalaz.Validation
import spray.routing.HttpServiceActor
import scalaz.NonEmptyList

/**
 * MailServiceActor: Actor que hace el ruteo Http
 */
class MailServiceActor extends Actor with HttpServiceActor {

  def receive = runRoute(mailServiceRoute)

  /**
   * Actor que atiende las solicitudes de envío de correo
   */
  val requestActor = actorRefFactory.actorOf(Props[RequestActor], "RequestActor")

  /**
   * Actor que maneja la persistencia de los estados de solicitud
   */
  val statusActor = actorRefFactory.actorOf(Props[StatusActor], "StatusActor")

  /**
   * Ruteo
   */
  val mailServiceRoute = {
    /**
     * Recepción de un correo
     * Recibe un Email en JSON
     * Devuelve el ticket (identificador único) de la solicitud
     */
    path("send") {
      post {
        entity(as[Email]) { email =>
          println("Request received: " + email.toString)
          val ticket = System.currentTimeMillis
          requestActor ! SendRequest(email.toEmailVO, ticket)
          println("Sending RequestTicket: " + ticket.toString)
          complete(Accepted, ticket.toString)
        }
      }
    } ~
    /**
     * Solicitudes de estado
     * Recibe un parámetro requestTicket 
     * Devuelve el estado de la solicitud
     */
      get {
        parameters("requestTicket") { requestTicketString =>
          try {
            val requestTicket = requestTicketString.toLong
            val timeout = Timeout(Duration.create(5, "seconds"))
            val validationFuture = Patterns.ask(statusActor, requestTicket, timeout).mapTo[Validation[NonEmptyList[Throwable], Option[RequestStatus]]]
            val validation = Await.result(validationFuture, Duration.create(5, "seconds"))
            validation.fold(
              fail => complete(InternalServerError, FailedRequestFactory(requestTicket, fail)),
              success => success match {
                case Some(_: SuccesfullRequest) => complete(OK)
                case Some(x: FailedRequest) => complete(InternalServerError, x)
                case None => complete(NotFound)
              })
          } catch {
            case _: NumberFormatException => complete(BadRequest)
          }
        }
      }
  }

}

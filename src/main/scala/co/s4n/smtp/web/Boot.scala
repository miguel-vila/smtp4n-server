package co.s4n.smtp.web

import akka.actor.{ActorSystem, Props}
import spray.can.server.SprayCanHttpServerApp

/**
 * Inicializa el servicio
 */
object Boot extends App with SprayCanHttpServerApp {
  val service = system.actorOf(Props[SMTPRestServiceActor], "Mail-service-actor")
  newHttpServer(service) ! Bind(interface = "localhost", port = 8070)  
}
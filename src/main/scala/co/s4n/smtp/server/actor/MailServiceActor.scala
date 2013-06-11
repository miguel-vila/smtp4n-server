package co.s4n.smtp.server.actor

import akka.actor.{ Actor, Props, OneForOneStrategy }
import co.s4n.smtp.server.message.{ SendRequest, RequestStatus }
import akka.actor.SupervisorStrategy.Restart
import scala.language.postfixOps
import scala.concurrent.duration._

/**
 * Actor que supervisa los actores y que unifica la lógica de la aplicación
 */
class MailServiceActor extends Actor{

  /**
   * Función para obtener los parametros de configuración
   */
  def getString(key: String) = context.system.settings.config.getString("co.s4n.smtp.server."+key)
  
   /**
   * Actor que atiende las solicitudes de envío de correo
   */
  val requestActor = context.actorOf(Props[RequestActor], "RequestActor")

  /**
   * Actor que maneja la persistencia de los estados de solicitud
   */
  val statusActor = context.actorOf(Props(new StatusActor(getString("riak.riakURL"), getString("riak.bucketName"))), "StatusActor")
  
  def receive = 
  {
    /**
     * Cuando recibe una solicitud de envío al actor de envío de correos
     * NO se hace forwarding porque se necesita que el requestActor 
     * le responda a este actor el estado de la solicitud. Así este actor
     * se lo pueda enviar al statusActor para que lo persista (siguiente case)
     */
    case sendRequest: SendRequest => requestActor ! sendRequest
    /**
     * Cuando recibe un estado de solicitud lo reenvía al statusActor para que
     * lo persista
     */
    case requestStatus: RequestStatus => statusActor ! requestStatus
    /**
     * Cuando recibe un ticket de solicitud reenvía el pedido al statusActor
     */
    case requestTicket: Long => statusActor forward requestTicket
  }
  
  /**
   * Estrategia del supervisor:
   * Esta estrategia solo es válida para el statusActor ya que el requestActor 
   * maneja sus excepciones enviandolas a este actor
   */
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: Throwable => Restart
  }
}
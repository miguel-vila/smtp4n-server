package co.s4n.smtp.server.actor

import akka.actor.{ Actor, ActorLogging }
import co.s4n.smtp.server.message.{ FailedRequest, SuccesfullRequest, RequestStatus }
import co.s4n.smtp.server.riak.RiakClient
import com.stackmob.scaliak.{ Scaliak, ScaliakConverter, ReadObject, WriteObject }
import com.twitter.chill.KryoInjection
import scalaz.{ Validation, NonEmptyList }

/**
 * Actor encargado de persistir los estados de las solicitudes
 * Extiende RiakClient para conectarse con el repositorio Riak
 */
class StatusActor(val riakURL: String, val bucketName: String) extends Actor{

  /**
   * Cliente de Riak
   */
  protected val riakClient = new RiakClient(riakURL, bucketName)
  
  /**
   * Recepci贸n de mensajes
   */
  def receive = 
  {
    /**
     * Cuando recibe un estado de solicitud lo persiste
     */
  	case requestStatus: RequestStatus => riakClient.store(requestStatus)
    
  	/**
  	 * Cuando recibe el ticket de una solicitud busca en el repositorio su respectivo estado
  	 * Devuelve un objeto del tipo Validation[Throwable, Option[RequestStatus]]
  	 */
    case ticket: Long => sender ! riakClient.fetch(ticket)
  }

}
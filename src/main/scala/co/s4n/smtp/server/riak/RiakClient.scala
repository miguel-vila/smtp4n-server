package co.s4n.smtp.server.riak

import com.stackmob.scaliak.{Scaliak, ScaliakConverter, ReadObject, WriteObject}
import com.typesafe.config.ConfigFactory
import co.s4n.smtp.server.message.RequestStatus
import com.twitter.chill.KryoInjection
import scalaz.{Validation, NonEmptyList}
import com.twitter.bijection.Bijection
import com.twitter.chill.KryoBijection

/**
 * RiakClient: Un cliente Riak para persistir y extraer objetos del tipo RequestStatus
 */
trait RiakClient {

//  def getConfigValue(key: String) = ConfigFactory.load().getString("co.s4n.smtp.server.riak." + key)

  /**
   * Cliente de Riak
   */
  val scaliakClient = Scaliak.httpClient( "http://192.168.1.198:8098/riak" )//getConfigValue("riakURL"))
  
  /**
   * Bucket donde se van a almacenar los estados de solicitud
   */
  val bucket = scaliakClient.bucket( "requestStatus" ) //getConfigValue("statusBucket"))
  .unsafePerformIO() valueOr { throw _ }

  /**
   * Convierte 
   */
  implicit val converter = ScaliakConverter.newConverter[RequestStatus](
    (o: ReadObject) => KryoInjection.invert(o.getBytes) match {
      case Some(x: RequestStatus) => Validation.success(x)
      case Some(_) => Validation.failure(new Exception("Valor almacenado no interpretable")).toValidationNel
      case None => Validation.failure(new Exception("No se pudo realizar la conversion")).toValidationNel
    },
    (o: RequestStatus) => WriteObject(o.requestTicket.toString, KryoInjection(o)))
    
  /**
   * Almacena un estado de solicitud
   */
  def store(requestStatus: RequestStatus) = bucket.store(requestStatus).unsafePerformIO() 
  
  /**
   * Devuelve un RequestStatus a partir de su número de ticket
   */
  def fetch(requestTicket: Long) = bucket.fetch[RequestStatus](requestTicket.toString).unsafePerformIO()
  
}
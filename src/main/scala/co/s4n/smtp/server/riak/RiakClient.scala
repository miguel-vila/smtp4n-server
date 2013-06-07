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
   * Convierte un RequestStatus en un arreglo de bytes y viceversa
   * Utiliza la librería chill para serializar y deserializar que
   * a su vez utiliza la librería Kryo de Java
   */
  implicit val converter = ScaliakConverter.newConverter[RequestStatus](
    (o: ReadObject) => KryoInjection.invert(o.getBytes) match {
    	/**
    	 * Cuando la deserialización resulta en un RequestStatus válido lo devuelve
    	 */
    	case Some(x: RequestStatus) => Validation.success(x)
    	/**
    	 * Cuando la deserialización resulta en un objeto que no es un RequestStatus falla
    	 * Esto nunca debería pasar y si sucede es porque el bucket no ha sido utilizado
    	 * exclusivamente para almacenar RequestStatus
    	 */
    	case Some(_) => Validation.failure(new Exception("Valor almacenado no interpretable")).toValidationNel
    	/**
    	 * Cuando no puede realizar la deserialización falla
    	 */
    	case None => Validation.failure(new Exception("No se pudo realizar la deserialización")).toValidationNel
    },
    (o: RequestStatus) => WriteObject(o.requestTicket.toString, KryoInjection(o)))
    
  /**
   * Almacena un estado de solicitud
   */
  def store(requestStatus: RequestStatus) = bucket.store(requestStatus).unsafePerformIO() 
  
  /**
   * Devuelve un scalaz.validation que en caso de ser exitoso contiene un RequestStatus a partir de su número de ticket
   */
  def fetch(requestTicket: Long) = bucket.fetch[RequestStatus](requestTicket.toString).unsafePerformIO()
  
  /**
   * Elimina todos los valores del bucket
   * Por ahora el cliente no permite hacer esto de una forma eficiente
   */
  def cleanUp() = bucket.listKeys.unsafePerformIO.valueOr(throw _).foreach(bucket.deleteByKey(_, false).unsafePerformIO.valueOr{throw _})
  
  /**
   * Retorna todos los valores del bucket
   * Por ahora el cliente no permite hacer esto de una forma eficiente
   */
  def fetchAll() = bucket.listKeys.unsafePerformIO.valueOr(throw _).map(key => fetch(key.toLong).valueOr(x => throw x.head)).map(x => x.get).toList
}
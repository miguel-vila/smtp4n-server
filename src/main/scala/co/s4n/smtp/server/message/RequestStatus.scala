package co.s4n.smtp.server.message

import com.stackmob.scaliak.{ScaliakConverter, ReadObject}
import spray.json.DefaultJsonProtocol
import scalaz.NonEmptyList

/**
 * RequestStatus: trait que representa el estado de una solicitud
 */
sealed trait RequestStatus {
  /**
   * Identificador único de la solicitud
   */
  val requestTicket: Long
}
/**
 * SuccesfullRequest: Estado que representa el estado exitoso de una solicitud
 */
case class SuccesfullRequest(val requestTicket: Long) extends RequestStatus
/**
 * FailedRequest: Estado que representa el estado fallido de una solicitud
 * 	exceptionName: Nombre de la excepción
 *  message: Mensaje de la excepción
 *  stackTrace: Traza de la excepción
 */
case class FailedRequest(val requestTicket: Long, val exceptionTrace: Array[Throwable]) extends RequestStatus

/**
 * Throwable: Throwable mandado en la ejecución
 * 	className: Nombre de la clase del Throwable
 *  message: mensaje del Throwable
 *  stackTrace: traza del Throwable
 */
case class Throwable(val className: String, val message: String, val stackTrace: Array[StackTraceElement])

/**
 * StackTraceElement: Elemento de la traza de la excepción
 * 	fileName: Nombre del archivo
 *  className: Nombre de la clase
 *  methodName: Nombre del método
 *  lineNumber: Número de línea de la excepción
 */
case class StackTraceElement(fileName: String, className: String, methodName: String, lineNumber: Int)

/**
 * Protocolo JSON para transformar un estado fallido desde y hacia JSON
 */
object FailedRequestJsonProtocol extends DefaultJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._
  implicit val stackTraceElementUnmarshaller = jsonFormat4(StackTraceElement)
  implicit val throwableUnmarshaller = jsonFormat3(Throwable)
  implicit val failedRequestUnmarshaller = jsonFormat2(FailedRequest)
}

/**
 * Creador de estados
 */
object FailedRequestFactory{
  
  /**
   * Extrae la traza de un java.lang.Throwable
   */
  def getStackTrace(throwable: java.lang.Throwable) : Array[StackTraceElement] = throwable.getStackTrace().map(
      elem => new StackTraceElement(elem.getFileName(), elem.getClassName(), elem.getMethodName(), elem.getLineNumber()))
      
  /**
   * Extrae la traza de causas de un java.lang.Throwable y las convierte en un Array[Throwable]
   */
  def getExceptionCauseTrace(throwable: java.lang.Throwable) : Array[Throwable] = {
    /**
     * Método auxiliar para hacer tail recursion
     */
    def getExceptionCauseTraceHelperFunction(throwable: java.lang.Throwable, acc: List[Throwable]) : List[Throwable] =
      if(throwable.getCause() == null)	toThrowable(throwable)::acc
      else	getExceptionCauseTraceHelperFunction(throwable.getCause(), toThrowable(throwable)::acc)
    getExceptionCauseTraceHelperFunction(throwable, Nil).toArray
  }
  
  /**
   * Convierte un java.lang.Throwable en un Throwable
   */
  def toThrowable(throwable: java.lang.Throwable) : Throwable = 
    new Throwable(throwable.getClass().getName(),
    				throwable.getMessage(),
    				getStackTrace(throwable))
  
  /**
   * Crea un FailedRequest a partir de un requestTicket y un Throwable de Java
   */
  def apply(requestTicket: Long, throwable: java.lang.Throwable) : FailedRequest = 
    new FailedRequest(requestTicket, getExceptionCauseTrace(throwable))

  /**
   * Crea un FailedRequest a partir de una lista de Throwables de Java
   */
  def apply(requestTicket: Long, throwableList: NonEmptyList[java.lang.Throwable]) : FailedRequest =
    apply(requestTicket, throwableList.head)
}
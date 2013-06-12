package co.s4n.smtp.server

import co.s4n.smtp.server.message.SendRequest
import co.s4n.smtp.server.message.Email
import co.s4n.smtp.server.message.SuccesfullRequest
import co.s4n.smtp.server.message.FailedRequest
import co.s4n.smtp.server.message.FailedRequestFactory

object TestEntityProvider {

  val correctEmail = Email("miguelvilag@gmail.com", "miguelvila@seven4n.com", List("miguelvilag@hotmail.com", "m.vila1775@uniandes.edu.co"), "Asunto", "Contenido")
  
  def correctSendRequest() = SendRequest(correctEmail.toEmailVO, System.currentTimeMillis())
  
  val failingEmail = Email("miguelvilag@gmail.com", "yonoexisto@yomenos.com", List("miguelvilag@hotmail.com", "m.vila1775@uniandes.edu.co"), "Asunto", "Contenido")

  def failingSendRequest() = SendRequest(failingEmail.toEmailVO, System.currentTimeMillis())
  
  def succesfullRequest() = SuccesfullRequest(System.currentTimeMillis)
  
  def failedRequest() = FailedRequestFactory(System.currentTimeMillis, new Exception("Mensaje excepci贸n"))
}
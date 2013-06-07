package co.s4n.smtp.server.message

import co.s4n.smtp.EmailVO

/**
 * Solicitud de envio de un correo
 * 	emailVO: El correo que se quiere enviar
 *  ticket: El ticket (identificador único) de la solicitud
 */
case class SendRequest(emailVO: EmailVO, ticket: Long)
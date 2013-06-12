package co.s4n.smtp.server.actor

import akka.testkit.{ TestKit, TestProbe, TestActorRef }
import akka.actor.ActorSystem
import org.scalatest.{ WordSpec, BeforeAndAfterAll }
import co.s4n.smtp.server.riak.RiakClient
import co.s4n.smtp.server.message.{ SendRequest, SuccesfullRequest, FailedRequest, Email }
import scala.concurrent.duration.Duration
import co.s4n.smtp.server.TestEntityProvider

/**
 * RequestActorSpec: Tests del RequestActor
 */
class RequestActorSpec extends TestKit(ActorSystem("RequestActorSpec")) with WordSpec with BeforeAndAfterAll{
  
  /**
   * Especificación del RequestActor
   */
  "RequestActor" should {
    val sender = TestProbe()
    val actorRef = TestActorRef[RequestActor]
    "Receive a SendRequest message that should be succesful and send a SuccesfulRequest message to the sender" in {
      val correctRequest = TestEntityProvider.correctSendRequest
      sender.send(actorRef, correctRequest) 
      sender.expectMsg(SuccesfullRequest(correctRequest.ticket))
    }
    "Receive a SendRequest message that should fail and send a FailedRequest message to the sender" in {
      val failingRequest = TestEntityProvider.failingSendRequest
      sender.send(actorRef, failingRequest) 
      sender.expectMsgType[FailedRequest]
    }
  }
  
  /**
   * Limpieza post-tests
   */
  override def afterAll() { system.shutdown() }

}
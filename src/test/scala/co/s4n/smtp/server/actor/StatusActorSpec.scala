package co.s4n.smtp.server.actor

import akka.testkit.{ TestKit, TestProbe, TestActorRef }
import akka.actor.ActorSystem
import org.scalatest.{ WordSpec, BeforeAndAfterAll }
import org.scalatest.matchers.MustMatchers
import co.s4n.smtp.server.riak.RiakClient
import co.s4n.smtp.server.message.{ RequestStatus, SendRequest, SuccesfullRequest, FailedRequest, FailedRequestFactory, Email }
import scala.concurrent.duration.Duration
import scalaz.Success
import co.s4n.smtp.server.riak.{ RiakTestClient, RiackTestConf }
import co.s4n.smtp.server.TestEntityProvider

/**
 * StatusActorTest: Extensión del StatusActor para poder testearlo
 */
class StatusActorTest extends StatusActor(RiackTestConf.riakURL, RiackTestConf.bucketName)

/**
 * StatusActorSpec: Tests del StatusActor
 */
class StatusActorSpec extends TestKit(ActorSystem("StatusActorSpec")) with WordSpec with MustMatchers with BeforeAndAfterAll
{
  /**
   * Cliente de Riak para verificar la correcta creación de entidads
   */
  val riakClient = new RiakTestClient
  
  /**
   * Limpieza pre-tests
   */
  override def beforeAll {
	  riakClient.cleanUp
  }
  
  /**
   * Especificación del StatusActor
   */
  "StatusActor" should {
    val sender = TestProbe()
    val actorRef = TestActorRef[StatusActorTest]
    "Receive a SuccesfulRequest message and store it" in {
      val succesfullRequest = TestEntityProvider.succesfullRequest
      sender.send(actorRef, succesfullRequest) 
      riakClient.fetch(succesfullRequest.requestTicket) must be (Success(Some(succesfullRequest)))
    }
    "Receive a query message for a SuccesfulRequest that exists in riak and return it" in {
        val succesfullRequest = TestEntityProvider.succesfullRequest
      	riakClient.store(succesfullRequest)
      	sender.send(actorRef, succesfullRequest.requestTicket)
        sender.expectMsg(Success(Some(succesfullRequest)))
    }
  }
  
  /**
   * Limpieza post-tests
   */
  override def afterAll() { riakClient.cleanUp ; riakClient.shutdown ; system.shutdown }
}
package co.s4n.smtp.server.riak

import co.s4n.smtp.server.actor.StatusActor

object RiackTestConf{
	val riakURL = "http://192.168.1.198:8098/riak"
	val bucketName = "testBucket"
}

class RiakTestClient extends RiakClient(RiackTestConf.riakURL, RiackTestConf.bucketName)
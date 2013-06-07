resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.5.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.2.0")

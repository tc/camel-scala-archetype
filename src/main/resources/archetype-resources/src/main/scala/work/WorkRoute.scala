package ${package}.work

import ${package}._
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.LoggingLevel

class WorkRoute extends RouteBuilder{
  val routeName = "route"

  override def configure(){

    errorHandler(loggingErrorHandler(getClass.toString))

    from(Config.queueScheme(routeName)).
    routeId(routeName).
    bean(classOf[Work]).
    log(LoggingLevel.DEBUG, "Received: ${body}").
    to("jdbc:database")
  }
}

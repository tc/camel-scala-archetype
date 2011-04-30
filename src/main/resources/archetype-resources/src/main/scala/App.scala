package ${package}

import org.apache.camel.impl.DefaultCamelContext

import org.apache.camel.impl.SimpleRegistry
import org.apache.camel.impl.MainSupport
import org.apache.camel.view.ModelFileGenerator

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.ProducerTemplate
import org.apache.camel.CamelContext

object App extends MainSupport{
  def main(args:Array[String]){
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT"))
    enableHangupSupport()
    run(args)
  }

  def setupContext:CamelContext = {
    val reg = setupRegistry

    val context = new DefaultCamelContext(reg)

    val activeMQComponent = Config.getQueueComponent("queue")
    context.addComponent("activemq", activeMQComponent)

    context.setHandleFault(true)
    // TODO Add route classes here
    context.addRoutes(new work.WorkRoute)
    context
  }

  def setupRegistry:SimpleRegistry = {
    val reg = new SimpleRegistry

    List("database").foreach{dbName => 
      val db = Config.getMySqlDataSource(dbName)
      reg.put(dbName, db)
    }
    reg
  }

  override def doStart{
    super.doStart
    postProcessContext()
    getCamelContexts().get(0).start()
  }
	
  override def doStop{
    super.doStop
    getCamelContexts().get(0).stop()
  }
	
	protected def findOrCreateCamelTemplate:ProducerTemplate ={
	  getCamelContexts().get(0).createProducerTemplate()
	}
	
	protected def getCamelContextMap:java.util.Map[String,CamelContext] = {
	  val answer = new java.util.HashMap[String, CamelContext]()
	  answer.put("camelContext", setupContext)
	  answer
	}
	
	protected def createModelFileGenerator:ModelFileGenerator ={
	  return null
  }
}


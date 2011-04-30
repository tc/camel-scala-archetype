package ${package}.work

import org.apache.camel.Handler
import org.apache.camel.language.XPath

class Work{
  @Handler
  def doWork(@XPath("/work") url:String):String = {

    //do work
    "INSERT SQL STATEMENT"
  }
}


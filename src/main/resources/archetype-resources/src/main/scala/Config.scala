package ${package}

import org.yaml.snakeyaml.Yaml
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.FileInputStream
import java.io.File

import javax.sql.DataSource
import com.mysql.jdbc.jdbc2.optional._
import org.apache.activemq.camel.component.ActiveMQComponent
import scala.collection.JavaConversions._
import org.apache.camel.Component

object Config{
  val environment = if(System.getenv("APP_ENV") != null) System.getenv("APP_ENV") else "development"

  def queueScheme(queueName:String):String = {
    val serviceName = "activemq:queue"
    val options = ""

    "%s:%s_%s".format(serviceName, queueName, environment, options)
  }

  def getMySqlDataSource(dbName:String):DataSource = {
    val dbSettings = getDatabaseSettings(dbName)

    val serverName = dbSettings.getOrElse("host", "localhost").asInstanceOf[String]
    val port = dbSettings.getOrElse("port", 3306).asInstanceOf[Int]
    val databaseName = dbSettings.get("database").asInstanceOf[String]
    val user = dbSettings.getOrElse("username", "root").asInstanceOf[String]
    val password = dbSettings.get("password").asInstanceOf[String]

    val dataSource = new MysqlConnectionPoolDataSource()
    dataSource.setUser(user)
    dataSource.setPassword(password)
    dataSource.setUrl("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true".format(serverName, port, databaseName))
    dataSource.setEncoding("UTF-8")
    dataSource
  }

  def getQueueComponent(brokerName:String):Component = {
    val queueSettingFilename = "src/main/resources/config/queue/" + brokerName + ".yml"
    val settings = readYamlSettings(queueSettingFilename)

    val connectorUri = settings.getOrElse("consumer_connector_uri", "nio://localhost:61616").asInstanceOf[String]
    
    val activeMQComponent = ActiveMQComponent.activeMQComponent(connectorUri)
    activeMQComponent.setUserName(settings.getOrElse("username", "").asInstanceOf[String])
    activeMQComponent.setPassword(settings.getOrElse("password", "").asInstanceOf[String])

    activeMQComponent
  }

  def getDatabaseSettings(dbName:String):java.util.HashMap[String,Any] = {
    LoggerFactory.getLogger(getClass.toString).info("Using %s environment for database: %s".format(environment, dbName))

    val dbSettingFilename = "src/main/resources/config/db/" + dbName + ".yml"
    readYamlSettings(dbSettingFilename)
  }

  def readYamlSettings(filename:String):java.util.HashMap[String,Any] = {
    val stream = new FileInputStream(new File(filename))
    val dbYaml = new Yaml
    val env = dbYaml.load(stream).asInstanceOf[java.util.HashMap[String,Any]]
    val settings = env.get(environment).asInstanceOf[java.util.HashMap[String,Any]]
    stream.close

    settings
  }
}


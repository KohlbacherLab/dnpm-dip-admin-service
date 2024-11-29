package de.dnpm.dip.admin.impl


import scala.concurrent.{
  Future,
  ExecutionContext
}
import cats.Monad
import de.dnpm.dip.util.Logging
import de.dnpm.dip.model.Site
import de.dnpm.dip.service.{
  Connector,
  ConnectionStatus
}
import de.dnpm.dip.connector.{
  FakeConnector,
  HttpConnector,
}
import de.dnpm.dip.connector.HttpMethod.GET
import de.dnpm.dip.admin.api._


class AdminServiceProviderImpl extends AdminServiceProvider
{
  override def getInstance: AdminService =
    AdminServiceImpl.instance
}


object AdminServiceImpl extends Logging
{
  
  private val connector =
    System.getProperty("dnpm.dip.connector.type","broker") match {
      case HttpConnector.Type(typ) =>
        HttpConnector(
          typ,
          {
            case _: StatusRequest => (GET, "/api/peer2peer/status", Map.empty) 
          }
        )

      case _ =>
        import scala.concurrent.ExecutionContext.Implicits._
        log.warn("Falling back to Fake Connector!")
        FakeConnector[Future]
    }


  private[impl] val instance =
    new AdminServiceImpl(connector)

}


class AdminServiceImpl(
  private val connector: Connector[Future,Monad[Future]]
)
extends AdminService
with Logging
{

  private val statusRequest =
    StatusRequest(Site.local)

/*  
  private val executor =
    Executors.newSingleThreadScheduledExecutor

  private val report =
    new AtomicReference(ConnectionReport(List.empty))

  // Schedule report compilation every minute
  executor.scheduleAtFixedRate(() => compileConnectionReport, 0, 60, SECONDS)


  private def compileConnectionReport = {
    log.debug("Compiling network connection status report")
    (connector ! statusRequest)
      .map(
        rs =>
          ConnectionReport(
            ConnectionStatus.from(rs)
              .toList
              .sortBy(_.site.display.get)  // call of .get safe here
          )
      )
      .onComplete { 
        case Success(r) => report.set(r)
        case Failure(t) => log.error("During compilation of network connection status report",t)
      }
  }

  override def connectionReport(
    implicit env: ExecutionContext
  ): Future[ConnectionReport] =
    Future.successful(report.get)
*/


  override def connectionReport(
    implicit env: ExecutionContext
  ): Future[ConnectionReport] =
    (connector ! statusRequest)
      .map(
        rs =>
          ConnectionReport(
            ConnectionStatus.from(rs)
              .toList
              .sortBy(_.site.display.get)  // call of .get safe here
          )
      )

}

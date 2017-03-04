import io.torchbearer.turkservice._
import org.scalatra._
import javax.servlet.ServletContext

import _root_.akka.actor.{ActorSystem, Props}
import io.torchbearer.turkservice.Task
import io.torchbearer.ServiceCore.TorchbearerDB
import io.torchbearer.turkservice.servlets.{InternalServlet, QuestionServlet}
import io.torchbearer.turkservice.resources.HitResource
import io.torchbearer.turkservice.tasks.{CompletedHitPollingTask, ProcessingPollingTask}

import scala.concurrent.Future
import scala.concurrent.duration._

class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()
  implicit val executor = system.dispatcher

  override def init(context: ServletContext) {
    // Start REST servers
    context.mount(new QuestionServlet(system), "/question/*")
    context.mount(new HitResource(system), "/hits/*")

    context.mount(new InternalServlet(system), "/internal/*")

    // Start continuous tasks
    //system.scheduler.schedule(30.seconds, 30.seconds, new PollingTask(system))
    if (sys.env.getOrElse("POLL_PROCESS_QUEUE", "true").toBoolean ) {
      system.scheduler.scheduleOnce(30.seconds, new ProcessingPollingTask(system))
    }

    if (sys.env.getOrElse("POLL_HIT_QUEUE", "true").toBoolean ) {
      system.scheduler.scheduleOnce(30.seconds, new CompletedHitPollingTask(system))
    }

    // Initialize core services
    TorchbearerDB.init()

    // Register HIT types
    SALIENCY_DETECTION.registerHitType
    OBJECT_DESCRIPTION.registerHitType
  }

  override def destroy(context:ServletContext) {
    system.shutdown
  }
}

import org.scalatra._
import javax.servlet.ServletContext
import _root_.akka.actor.ActorSystem
import io.torchbearer.ServiceCore.TorchbearerDB
import io.torchbearer.turkservice.servlets.{InternalServlet, QuestionServlet}
import io.torchbearer.turkservice.polling._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  override def init(context: ServletContext) {
    // Initialize core services
    TorchbearerDB.init()

    // Start REST servers
    context.mount(new QuestionServlet(system), "/question/*")
    context.mount(new InternalServlet(system), "/internal/*")

    // Start StepFunction workers for saliency and description tasks
    //system.scheduler.schedule(30.seconds, 30.seconds, new PollingTask(system))
    system.scheduler.scheduleOnce(5.seconds, new TurkServiceDescriptionWorker(system))
    system.scheduler.scheduleOnce(5.seconds, new TurkServiceSaliencyWorker(system))

    // Start completed hit polling workers
    system.scheduler.scheduleOnce(5.seconds, new CompletedDescriptionHitPollingWorker(system))
    system.scheduler.scheduleOnce(5.seconds, new CompletedSaliencyHitPollingWorker(system))
    system.scheduler.scheduleOnce(5.seconds, new CompletedVerificationHitPollingWorker(system))
  }

  override def destroy(context:ServletContext) {
    system.shutdown
  }
}

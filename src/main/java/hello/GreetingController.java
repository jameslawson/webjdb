package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class GreetingController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private JDIDemo debugger;

    public void onConnect(){
        // debugger.startDebugging(debugger.connect(5050, this));
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        // Thread.sleep(3000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }

    // @ClientRequest
    @MessageMapping("/start")
    public void startDebugging(){
        debugger.startDebugging(debugger.connect(5050, this));
    }

    // @ClientRequest
    @MessageMapping("/step")
    public void step(){
        debugger.requestStep();
        debugger.resume();
    }

    // @ClientRequest
    @MessageMapping("/suspend")
    public void suspend(){
        debugger.suspend();
    }

    // @ClientRequest
    @MessageMapping("/resume")
    public void resume(){
        debugger.resume();
    }

    // @ClientRequest
    @MessageMapping("/printframe")
    public void requestPrintFrame() {
        sendStackFrame();
    }


    // @DebuggerEvent
    public void onStep() {
        sendStackFrame();
    }

    public void sendStackFrame() {
        template.convertAndSend("/topic/stackframe", debugger.getStackFrameEvaluation());
    }

    public void fireGreeting(String message) {
        System.out.println(message);
        template.convertAndSend("/topic/greetings", new Greeting(message));
    }

}

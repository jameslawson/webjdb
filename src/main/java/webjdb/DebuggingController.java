package webjdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class DebuggingController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private Debugger debugger;

    public void onConnect(){
        // debugger.startDebugging(debugger.connect(5050, this));
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
    public void onStepHit(String lineKey) {
        sendStackFrame();
        sendMiniMessage("Step hit. " + lineKey);
    }

    // @DebuggerEvent
    public void onBreakpointHit(String lineKey) {
        sendStackFrame();
        sendMiniMessage("Step hit. " + lineKey);
    }

    private void sendStackFrame() {
        template.convertAndSend("/topic/stackframe", debugger.getStackFrameEvaluation());
    }

    private void sendMiniMessage(String message) {
        System.out.println(message);
        template.convertAndSend("/topic/minimessage", new MiniMessage(message));
    }

}

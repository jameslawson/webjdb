package hello;

import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class StompConnectEvent implements ApplicationListener<SessionConnectedEvent>
{
    private GreetingController controller;

    @Autowired
    public StompConnectEvent(final GreetingController greetingController) {
        controller = greetingController;
    }

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        System.out.println("CONNNECTED!!!!");
        controller.onConnect();
        // StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        // String company = sha.getNativeHeader("company").get(0);
        // System.out.println("Connect event sessionID: " + sha.getSessionId());
    }
}

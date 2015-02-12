package webjdb;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;

public class EventBroadcaster implements Runnable 
{
    private EventObserving observer;
    private EventQueue queue;

    public EventBroadcaster(EventObserving o, EventQueue q) {
        observer = o;
        queue = q;
    }

    public void run() {
        while (true) { 
            try {
                EventSet events = queue.remove();
                for (Event e : events) {
                    if (e instanceof VMDeathEvent) {
                        observer.onVMDeathEvent((VMDeathEvent)e);
                        return;
                    } 
                    if (e instanceof VMDisconnectEvent) {
                        observer.onVMDisconnectEvent((VMDisconnectEvent)e);
                        return;
                    }
                    else if (e instanceof ClassPrepareEvent) {
                        observer.onClassPrepareEvent((ClassPrepareEvent)e);
                    } 
                    else if (e instanceof BreakpointEvent) {
                        observer.onBreakpointEvent((BreakpointEvent)e);
                    }
                    else if (e instanceof StepEvent) {
                        observer.onStepEvent((StepEvent)e);
                    }
                }
                events.resume();
            } catch (InterruptedException exc) {
            }
        }
    }

}

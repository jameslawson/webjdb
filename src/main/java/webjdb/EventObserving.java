package webjdb;

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;

public interface EventObserving {

    public void onVMDeathEvent(VMDeathEvent event);
    public void onVMDisconnectEvent(VMDisconnectEvent event);
    public void onClassPrepareEvent(ClassPrepareEvent event);
    public void onBreakpointEvent(BreakpointEvent event);
    public void onStepEvent(StepEvent event);
    
}

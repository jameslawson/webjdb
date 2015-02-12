package webjdb;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Location;
import java.util.List;
import java.util.ArrayList;

public class EventRequestor 
{
    EventRequestManager manager;

    public EventRequestor(EventRequestManager requestManager) {
        manager = requestManager;
    }

    public void requestClassPrepare()
    {
        ClassPrepareRequest req = manager.createClassPrepareRequest();
        req.addClassFilter("Target");
        req.setEnabled(true);
        req.enable();
    }

    public void requestBreakpoint(Location loc)
    {
        BreakpointRequest breakpointRequest = manager.createBreakpointRequest(loc);
        breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        breakpointRequest.setEnabled(true);
    }

    public void requestStep(ThreadReference thread) 
    {
        deleteStepRequests();
        StepRequest req = manager.createStepRequest(thread,
                StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        req.addClassFilter("Target");
        req.addCountFilter(1);
        req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        req.enable();
    }

    public void deleteStepRequests() 
    {
        List<StepRequest> requests = manager.stepRequests();
        if (requests.size() > 0) {
            List<StepRequest> toDelete = new ArrayList<StepRequest>(requests.size());
            for (StepRequest r : requests) {
                ThreadReference thread = r.thread();
                if (thread.status() != ThreadReference.THREAD_STATUS_UNKNOWN) {
                    toDelete.add(r);
                }
            }
            manager.deleteEventRequests(toDelete);
        }
    }
}

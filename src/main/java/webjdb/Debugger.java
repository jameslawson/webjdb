package webjdb;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ClassType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;

import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.event.StepEvent;

import java.io.InputStream;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Debugger
{

    private DebuggingController controller;
    private VirtualMachine virtualMachine;
    private ThreadReference currentThread;
    private boolean suspended = true;

    public VirtualMachine connect(int port, DebuggingController controller) 
    {
        this.controller = controller;
        // VirtualMachineManager has the list of connections
        VirtualMachineManager manager = Bootstrap.virtualMachineManager();

        // iterate over all connections until
        // until you find a connector supporting transport dt_socket.
        AttachingConnector connector = null;
        List<AttachingConnector> connectors = manager.attachingConnectors();
        for (AttachingConnector c: connectors) {
            if (c.transport().name().equals("dt_socket")) {
                connector = c;
                break;
            }
        }
        if (connector == null) { System.out.println("Could not find dt_socket"); return null; }

        // Get the port Connector.Argument of the AttachingConnector and 
        // set it to the port on which your target application is listening.
        // Attach to the AttachingConnector and get an instance of a VirtualMachine.
        Map<String, Connector.Argument> params = connector.defaultArguments();
        Connector.IntegerArgument portArg = (Connector.IntegerArgument)params.get("port");
        portArg.setValue(port);
        VirtualMachine vm;
        try { 
            vm = connector.attach(params); 
            if (vm == null) { System.out.println("Failed to connect to VM."); return null; }
            System.out.println("Attached to process '" + vm.name() + "'");
            virtualMachine = vm;
            List<ThreadReference> threads = virtualMachine.allThreads();
            ThreadReference thread = null;
            for (ThreadReference t : threads) { 
                if (t.uniqueID() == 1) {
                    currentThread = t;
                }
            }
            if (currentThread == null) { System.out.println("Could not find thread."); return null; }
            return vm;
        } 
        catch (IOException e) { }
        catch (IllegalConnectorArgumentsException e) { }
        // System.out.println("description='" + vm.description() + "'");
        // System.out.println("JVM version='" + vm.version() + "'");
        return null;
    }

    public void resume() {
        virtualMachine.resume();
    }

    public void suspend() {
        virtualMachine.suspend();
    }

    public void requestStep() 
    {
        deleteStepRequests();
        System.out.println("STEP REQUESTED");
        EventRequestManager requestManager = virtualMachine.eventRequestManager();
        StepRequest req = requestManager.createStepRequest(currentThread,
                StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        req.addClassFilter("Target");
        req.addCountFilter(1);
        req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        req.enable();
        System.out.println("STEP REQUEST SENT");
    }

    public void deleteStepRequests() {
        EventRequestManager manager = virtualMachine.eventRequestManager();
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

    public StackFrameEvaluation getStackFrameEvaluation() {
        try {
            StackFrame frame = currentThread.frame(0);
            List<LocalVariable> vars = frame.visibleVariables();
            StackFrameEvaluation ret = new StackFrameEvaluation();
            for (LocalVariable v : vars) {
                Value val = frame.getValue(v);
                if (val instanceof IntegerValue) {
                    int eval = ((IntegerValue)val).value();
                    ret.addVariableEvaluation(v.name(), Integer.toString(eval)); 
                } else {
                    ret.addVariableEvaluation(v.name(), "?"); 
                }
            }
            return ret;
        } catch (AbsentInformationException e) {
        } catch (IncompatibleThreadStateException e) {
        }
        return null;
    }

    
    public void startDebugging(VirtualMachine vm)
    {
        this.resume();
        EventRequestManager requestManager = vm.eventRequestManager();
        ClassPrepareRequest req = requestManager.createClassPrepareRequest();
        req.addClassFilter("Target");
        req.setEnabled(true);
        req.enable();

        EventQueue queue = vm.eventQueue();
        while (true) { 
            try {
                EventSet events = queue.remove();
                for (Event e : events) {
                    if (e instanceof VMDeathEvent) {
                        System.out.println("DISCONNECTED (VMDEATH)");
                        return;
                    } 
                    if (e instanceof VMDisconnectEvent) {
                        System.out.println("DISCONNECTED (VMDISCONNECT)");
                        return;
                    }
                    else if (e instanceof ClassPrepareEvent) 
                    {
                        ClassPrepareEvent event = (ClassPrepareEvent) e;
                        ClassType ref = (ClassType)event.referenceType();
                        System.out.println("The class loaded is " + ref.name());
                        //
                        List<Location> locs = ref.locationsOfLine(3);
                        if (locs.size() == 0) {
                            System.out.println("Adding Breakpoint failed");
                            return;
                        }
                        Location loc = locs.get(0);
                        EventRequestManager erm = vm.eventRequestManager();
                        BreakpointRequest breakpointRequest = erm.createBreakpointRequest(loc);
                        breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                        breakpointRequest.setEnabled(true);
                    } 
                    else if (e instanceof BreakpointEvent) 
                    {
                        System.out.println("BREAKPOINT EVENT");
                        BreakpointEvent event = (BreakpointEvent)e;
                        ThreadReference thread = event.thread();
                        System.out.println("target thread id:" + thread.uniqueID());
                        if (thread.frameCount() == 0) {
                            System.out.println("Adding Breakpoint failed 2");
                        }
                        StackFrame frame = thread.frame(0);
                        List<LocalVariable> vars = frame.visibleVariables();
                        final String sourcePath = event.location().sourcePath();
                        final int lineNumber = event.location().lineNumber();
                        final String method = event.location().method().toString();
                        final String lineKey = sourcePath + ":" + method  + ":" + lineNumber;
                        this.suspend();
                        controller.onBreakpointHit(lineKey);
                    }
                    else if (e instanceof StepEvent)
                    {
                        StepEvent event = (StepEvent)e;
                        System.out.println("STEP EVENT.");
                        final String sourcePath = event.location().sourcePath();
                        final int lineNumber = event.location().lineNumber();
                        final String method = event.location().method().toString();
                        final String lineKey = sourcePath + ":" + method  + ":" + lineNumber;
                        this.suspend();
                        controller.onStepHit(lineKey);
                    }
                }
                events.resume();
            } catch (InterruptedException e) {
            } catch (AbsentInformationException e) {
            } catch (IncompatibleThreadStateException e) {
            }
        }
    }
}


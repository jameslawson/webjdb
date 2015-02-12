package webjdb;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ClassType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Service
@Scope("singleton")
public class Debugger implements EventObserving
{
    private DebuggingController controller;
    private VirtualMachine virtualMachine;
    private ThreadReference currentThread;
    private EventBroadcaster broadcaster;
    private EventRequestor requestor;
    private FrameEvaluator evaluator;

    public VirtualMachine connect(int port, DebuggingController controller) 
    {
        this.controller = controller;

        // VirtualMachineManager has the list of connections
        // iterate over all connections until
        // until you find a connector supporting transport dt_socket.
        VirtualMachineManager manager = Bootstrap.virtualMachineManager();
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
                if (t.uniqueID() == 1) currentThread = t;
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

    public void startDebugging(VirtualMachine vm) 
    {
        broadcaster = new EventBroadcaster(this, virtualMachine.eventQueue());
        requestor = new EventRequestor(virtualMachine.eventRequestManager());
        evaluator = new FrameEvaluator();
        Thread broadcasting = new Thread(broadcaster);
        broadcasting.start();
        resume();
        requestor.requestClassPrepare();
    }

    public void resume() {
        virtualMachine.resume();
    }

    public void suspend() {
        virtualMachine.suspend();
    }

    public void requestStep() {
        requestor.requestStep(currentThread);
    }

    public StackFrameEvaluation getStackFrameEvaluation() {
        return evaluator.getStackFrameEvaluation(currentThread);
    }

    public void onVMDeathEvent(VMDeathEvent event) {
        System.out.println("DISCONNECTED (VMDEATH)");
    }

    public void onVMDisconnectEvent(VMDisconnectEvent event) {
        System.out.println("DISCONNECTED (VMDISCONNECT)");
    }

    public void onClassPrepareEvent(ClassPrepareEvent event) 
    {
        try {
            System.out.println("CLASS PREPARE EVENT.");
            ClassType theclass = (ClassType)event.referenceType();
            List<Location> locations = theclass.locationsOfLine(3);
            if (locations.size() == 0) {
                System.out.println("Adding Breakpoint failed");
                return;
            }
            requestor.requestBreakpoint(locations.get(0));
        } catch (AbsentInformationException e) {
        }
    }

    public void onBreakpointEvent(BreakpointEvent event) 
    {
        try {
            System.out.println("BREAKPOINT EVENT");
            ThreadReference thread = event.thread();
            StackFrame frame = thread.frame(0);
            List<LocalVariable> vars = frame.visibleVariables();
            final String sourcePath = event.location().sourcePath();
            final int lineNumber = event.location().lineNumber();
            final String method = event.location().method().toString();
            final String lineKey = sourcePath + ":" + method  + ":" + lineNumber;
            this.suspend();
            System.out.println(lineKey);
            controller.onBreakpointHit(lineKey);
        } catch (AbsentInformationException e) {
        } catch (IncompatibleThreadStateException e) {
        }
    }

    public void onStepEvent(StepEvent event) {
        try {
            System.out.println("STEP EVENT");
            final String sourcePath = event.location().sourcePath();
            final int lineNumber = event.location().lineNumber();
            final String method = event.location().method().toString();
            final String lineKey = sourcePath + ":" + method  + ":" + lineNumber;
            this.suspend();
            controller.onStepHit(lineKey);
        } catch (AbsentInformationException e) {
        }
    }

}


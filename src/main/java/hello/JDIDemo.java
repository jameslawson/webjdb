package hello;

import java.util.List;
import java.util.Map;
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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class JDIDemo
{

    private GreetingController greetingController;

    public VirtualMachine connect(int port, GreetingController controller) 
    {
        greetingController = controller;
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

        vm.resume();
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
                    if (e instanceof VMDeathEvent || 
                            e instanceof VMDisconnectEvent) {
                        return;
                            }
                    else if (e instanceof ClassPrepareEvent) 
                    {
                        System.out.println("YOOOOOO");
                        ClassPrepareEvent event = (ClassPrepareEvent) e;
                        ClassType ref = (ClassType)event.referenceType();
                        System.out.println("The class loaded is " + ref.name());
                        //
                        List<Location> locs = ref.locationsOfLine(4);
                        if (locs.size() == 0) {
                            System.out.println("Adding Breakpoint failed");
                            return;
                        }
                        Location loc = locs.get(0);
                        EventRequestManager erm = vm.eventRequestManager();
                        BreakpointRequest breakpointRequest = erm.createBreakpointRequest(loc);
                        breakpointRequest.setEnabled(true);
                    } 
                    else if (e instanceof BreakpointEvent) 
                    {
                        BreakpointEvent event = (BreakpointEvent)e;
                        ThreadReference thread = event.thread();
                        if (thread.frameCount() == 0) {
                            System.out.println("Adding Breakpoint failed 2");
                        }
                        StackFrame frame = thread.frame(0);
                        List<LocalVariable> vars = frame.visibleVariables();
                        for (LocalVariable v : vars) {
                            if (v.name().equals("test")) {
                                Value val = frame.getValue(v);
                                if (val instanceof IntegerValue) {
                                    System.out.println("FOOOO");
                                    int eval = ((IntegerValue)val).value();
                                    System.out.println("test" + " = '" + eval + "'");
                                    greetingController.fireGreeting("test " + "= '" + eval + "'");
                                }
                            }
                        }
                        vm.resume();
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


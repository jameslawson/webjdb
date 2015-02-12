package webjdb;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import java.util.List;

public class FrameEvaluator 
{
    public StackFrameEvaluation getStackFrameEvaluation(ThreadReference thread) 
    {
        StackFrameEvaluation ret = new StackFrameEvaluation();
        try {
            StackFrame frame = thread.frame(0);
            List<LocalVariable> vars = frame.visibleVariables();
            for (LocalVariable v : vars) {
                Value val = frame.getValue(v);
                if (val instanceof IntegerValue) {
                    int eval = ((IntegerValue)val).value();
                    ret.addVariableEvaluation(v.name(), Integer.toString(eval)); 
                } else {
                    ret.addVariableEvaluation(v.name(), "?"); 
                }
            }
        } catch (AbsentInformationException e) {
        } catch (IncompatibleThreadStateException e) {
        }
        return ret;
    }
}

package hello;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class StackFrameEvaluation {

    private Map<String, String> variableEvaluations = new HashMap<String, String>();

    public Map<String, String> getVariableEvaluations() {
        return Collections.unmodifiableMap(variableEvaluations);
    }

    public void addVariableEvaluation(String variable, String evaluation) {
        variableEvaluations.put(variable, evaluation);
    }

}

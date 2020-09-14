package com.ats.script.actions;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;

import java.util.ArrayList;

public class ActionSystemPropertyGet extends ActionProperty {

    public static final String SCRIPT_LABEL = "property-get";

    public ActionSystemPropertyGet() { }

    public ActionSystemPropertyGet(Script script, boolean stop, ArrayList<String> options, String name, Variable variable) {
        super(script, stop, options, name, variable, null);
    }

    public ActionSystemPropertyGet(Script script, boolean stop, int maxTry, int delay, String name, Variable variable) {
        super(script, stop, maxTry, delay, null, name, variable);
    }

    @Override
    public void terminateExecution(ActionTestScript ts) {
        super.terminateExecution(ts);

        if (status.isPassed()) {

            String name = getName();
            final String attributeValue = ts.getCurrentChannel().getSysProperty(status, name);
            status.endDuration();

            if (attributeValue == null) {
                status.setError(ActionStatus.ATTRIBUTE_NOT_SET, "attribute '" + name + "' not found", name);
                ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), name);
            } else {
                status.setMessage(attributeValue);
                updateVariableValue(attributeValue);
                ts.getRecorder().update(0, status.getDuration(), name, attributeValue);
            }
        }
    }
}

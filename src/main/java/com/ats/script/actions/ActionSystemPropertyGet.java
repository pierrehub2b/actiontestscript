package com.ats.script.actions;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;

public class ActionSystemPropertyGet extends ActionReturnVariableArray {

    public static final String SCRIPT_LABEL = "property-get";
    
    private String name;
    
    public ActionSystemPropertyGet() { }

    public ActionSystemPropertyGet(Script script, Variable variable, String name) {
        super(script, variable);
        setName(name);
    }
    
    @Override
    public void execute(ActionTestScript ts, String testName, int line) {
        super.execute(ts, testName, line);
    
        if (status.isPassed()) {
        
            String name = getName();
            final String attributeValue = ts.getCurrentChannel().getSysProperty(status, name);
            status.endDuration();
        
            if (attributeValue == null) {
                status.setError(ActionStatus.ATTRIBUTE_NOT_SET, "attribute '" + name + "' not found", name);
                ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), name);
            } else {
                status.setMessage(attributeValue);
                
                getVariable().setData(attributeValue);
                
                ts.getRecorder().update(0, status.getDuration(), name, attributeValue);
            }
        }
    }
    
    @Override
    public StringBuilder getJavaCode() {
        StringBuilder builder = super.getJavaCode();
        builder.append(", \"")
                .append(name)
                .append("\", ")
                .append(getVariable().getName())
                .append(")");
        return builder;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    private Variable getVariable() {
        if (getVariables().size() > 0) {
            return getVariables().get(0);
        } else {
            return null;
        }
    }
}

package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionPropertySet extends Action {

    public static final String SCRIPT_LABEL = "property-set";

    private String name;
    private CalculatedValue value;

    public ActionPropertySet() { }

    public ActionPropertySet(Script script, String name, String value) {
        super(script);
        setName(name);
        setValue(new CalculatedValue(script, value));
    }
    
    public ActionPropertySet(Script script, String name, CalculatedValue value) {
        super(script);
        setName(name);
        setValue(value);
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean execute(ActionTestScript ts, String testName, int line) {
        super.execute(ts, testName, line);

        Channel currentChannel = ts.getCurrentChannel();
        if (currentChannel != null) {
            currentChannel.setSysProperty(getName(), getValue().getCalculated());
            
            status.endAction();
            ts.getRecorder().update(0, status.getDuration(), getName(), getValue().getCalculated());
        }
        
		return true;
    }

    //--------------------------------------------------------
    // getters and setters for serialization
    //--------------------------------------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CalculatedValue getValue() { return value; }
    public void setValue(CalculatedValue value) { this.value = value; }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Code Generator
    //---------------------------------------------------------------------------------------------------------------------------------
    
    @Override
    public StringBuilder getJavaCode() {
        StringBuilder builder = super.getJavaCode();
        builder.append("\"")
                .append(name)
                .append("\"")
                .append(", ")
                .append(value.getJavaCode())
                .append(")");
        return builder;
    }
}

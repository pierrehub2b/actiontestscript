package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;

public class ActionSystemPropertySet extends ActionSystem {

    public static final String SCRIPT_LABEL = "property-set";

    private String name;
    private String value;

    public ActionSystemPropertySet() { }

    public ActionSystemPropertySet(Script script, String name, String value) {
        super(script);
        setName(name);
        setValue(value);
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void execute(ActionTestScript ts, String testName, int line) {
        super.execute(ts, testName, line);

        Channel currentChannel = ts.getCurrentChannel();
        if (currentChannel != null) {
            currentChannel.setSysProperty(getName(), getValue());
        }
    }

    //--------------------------------------------------------
    // getters and setters for serialization
    //--------------------------------------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Code Generator
    //---------------------------------------------------------------------------------------------------------------------------------


    @Override
    public StringBuilder getJavaCode() {
        StringBuilder builder = super.getJavaCode();
        builder.append("\"").append(name).append("\"").append(", ").append("\"").append(value).append("\"").append(")");
        return builder;
    }
}

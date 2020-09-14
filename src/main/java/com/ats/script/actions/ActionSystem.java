package com.ats.script.actions;

import com.ats.script.Script;

public abstract class ActionSystem extends Action {

    public static final String SCRIPT_LABEL = "system";

    public ActionSystem() {}

    public ActionSystem(Script script) {
        super(script);
    }
}

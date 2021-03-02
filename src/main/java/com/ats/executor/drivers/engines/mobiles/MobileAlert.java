package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.ats.element.DialogBox;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import org.openqa.selenium.NoAlertPresentException;

import java.util.ArrayList;
import java.util.List;

public class MobileAlert extends DialogBox {

    private final AtsMobileElement dialog;
    private final MobileDriverEngine engine;

    public MobileAlert(MobileDriverEngine engine) {
        super(engine);

        List<AtsMobileElement> elements = engine.getDialogBox();
        if (elements.size() == 0) {
            throw new NoAlertPresentException();
        } else {
            this.engine = engine;
            this.dialog = elements.get(0);
        }
    }

    private List<AtsMobileElement> loadButtons() {
        final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();
        engine.loadElementsByTag(dialog, "Button", list);
        return list;
    }

    public String getText() {
        return "";
    }

    public String getTitle() {
        return "";
    }

    public void clickButtonText(String text, ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        for (AtsMobileElement button : buttons) {
            if (button.getAttribute("label").equalsIgnoreCase(text)) {
                executeRequest(button);
                break;
            }
        }
    }

    public void clickButtonId(String id, ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        for (AtsMobileElement button : buttons) {
            if (button.getAttribute("id").equalsIgnoreCase(id)) {
                executeRequest(button);
                break;
            }
        }
    }

    public void clickButtonAtIndex(int index, ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        final AtsMobileElement button = buttons.get(index);
        if (button != null) {
            executeRequest(button);
        } else {

        }
    }

    private void executeRequest(AtsMobileElement element) {
        String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight();
        engine.executeRequest(MobileDriverEngine.ELEMENT,element.getId(), MobileDriverEngine.ALERT_TAP, coordinates);
    }
}

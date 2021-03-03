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
            if (button.getAttribute("text").equalsIgnoreCase(text)) {
                executeTapRequest(button);
                break;
            }
        }
    }

    public void clickButtonId(String id, ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        for (AtsMobileElement button : buttons) {
            if (button.getAttribute("identifier").equalsIgnoreCase(id)) {
                executeTapRequest(button);
                break;
            }
        }
    }

    public void clickButtonAtIndex(int index, ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        
        if (buttons.size() > index - 1) {
            final AtsMobileElement button = buttons.get(index - 1);
            executeTapRequest(button);
        } else {
        
        }
    }

    private void executeTapRequest(AtsMobileElement element) {
        String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight();
        engine.executeRequest(MobileDriverEngine.ALERT,element.getId(), MobileDriverEngine.TAP, "0", "0", coordinates);
    }
    
    public void sendKeys(String txt, int index) {
        final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();
        engine.loadElementsByTag(dialog, "TextField", list);
        
        // executeTapRequest(element);
        // engine.executeRequest(MobileDriverEngine.ALERT, element.getId(), MobileDriverEngine.INPUT, txt);
    }
    
    public void sendKeys1(String txt, int index) {
        final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();
        engine.loadElementsByTag(dialog, "TextField", list);
        
        // executeTapRequest(element);
        // engine.executeRequest(MobileDriverEngine.ALERT, element.getId(), MobileDriverEngine.INPUT, txt);
    }
    
    
    public void sendKeys2(String txt, int index) {
        final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();
        engine.loadElementsByTag(dialog, "TextField", list);
        
        // executeTapRequest(element);
        // engine.executeRequest(MobileDriverEngine.ALERT, element.getId(), MobileDriverEngine.INPUT, txt);
    }
}

package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.ats.element.DialogBox;
import com.ats.element.TestElementDialog;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import org.openqa.selenium.NoAlertPresentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MobileAlert extends DialogBox {

    private static final Map<String,String[]> buttonText = Map.of(
            TestElementDialog.ACCEPT, new String[]{"accept", "accepter", "ok", "oui", "yes", "true", "vrai", "si", "validar", "aceptar", "valider", "validate"},
            TestElementDialog.DISMISS, new String[]{"dismiss", "cancel", "cancelar", "annuler", "non", "no", "false", "faux", "falso", "deny", "decline", "refuser"}
            );

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

    public void defaultButton(ActionStatus status) {
        clickButtonAtIndex(1, status);
    }

    public void dismiss(ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        for (AtsMobileElement button : buttons) {
            if (Arrays.asList(buttonText.get(TestElementDialog.DISMISS)).contains(button.getAttribute("text").toLowerCase())) {
                executeTapRequest(button);
                break;
            }
        }
    }

    public void accept(ActionStatus status) {
        final List<AtsMobileElement> buttons = loadButtons();
        for (AtsMobileElement button : buttons) {
            if (Arrays.asList(buttonText.get(TestElementDialog.ACCEPT)).contains(button.getAttribute("text").toLowerCase())) {
                executeTapRequest(button);
                break;
            }
        }
    }

    private List<AtsMobileElement> loadButtons() {
        final List<AtsMobileElement> list = new ArrayList<>();
        engine.loadElementsByTag(dialog, "Button", list);
        return list;
    }

    private List<AtsMobileElement> loadTextFields() {
        final List<AtsMobileElement> list = new ArrayList<>();
        engine.loadElementsByTag(dialog, "TextField", list);
        return list;
    }

    public String getText() {
        return dialog.getAttribute("label");
    }

    public String getTitle() {
        return dialog.getAttribute("label");
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
        
        if (index > 0 && buttons.size() > index - 1) {
            final AtsMobileElement button = buttons.get(index - 1);
            executeTapRequest(button);
        }
    }

    public void sendKeys(String txt) {
        sendKeys(txt, 1);
    }
    
    public void sendKeys(String txt, int index) {
        final List<AtsMobileElement> textFields = loadTextFields();
        if (index > 0 && textFields.size() > index - 1) {
            final AtsMobileElement element = textFields.get(index - 1);
            executeInputRequest(element, txt);
        }
    }

    public void sendKeys(String txt, String identifier) {
        final List<AtsMobileElement> textFields = loadTextFields();
        for (AtsMobileElement element : textFields) {
            if (element.getAttribute("identifier").equalsIgnoreCase(identifier)) {
                executeInputRequest(element, txt);
                break;
            }
        }
    }

    private void executeTapRequest(AtsMobileElement element) {
        String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight();
        engine.executeRequest(MobileDriverEngine.ALERT,element.getId(), MobileDriverEngine.TAP, "0", "0", coordinates);
    }

    private void executeInputRequest(AtsMobileElement element, String text) {
        double x = element.getX() + (element.getWidth() / 2);
        double y = element.getY() + (element.getHeight() / 2);
        String coordinates = x + ";" + y;
        engine.executeRequest(MobileDriverEngine.ALERT, element.getId(), MobileDriverEngine.INPUT, coordinates, text);
    }
}

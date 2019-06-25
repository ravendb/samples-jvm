package net.ravendb.demo.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MainMenu extends VerticalLayout {

    public MainMenu() {
        this.init();
    }

    private void init() {

        setWidth("20%");

        Button patientButton = new Button("Patients", new Icon(VaadinIcon.MALE), e -> {
            UI.getCurrent().navigate("patient");
        });
        add(patientButton);

        Button conditionButton = new Button("Visits", new Icon(VaadinIcon.PENCIL), e -> {
            UI.getCurrent().navigate("visits");
        });
        add(conditionButton);

        Button doctorButton = new Button("Doctor", new Icon(VaadinIcon.PAPERCLIP), e -> {
            UI.getCurrent().navigate("doctor");
        });
        add(doctorButton);

        Button visitsButton = new Button("Condition", new Icon(VaadinIcon.PENCIL), e -> {
            UI.getCurrent().navigate("condition");
        });
        add(visitsButton);

    }

}

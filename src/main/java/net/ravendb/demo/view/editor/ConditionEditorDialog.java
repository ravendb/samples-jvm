package net.ravendb.demo.view.editor;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

import net.ravendb.client.exceptions.ConcurrencyException;
import net.ravendb.demo.model.Condition;
import net.ravendb.demo.presenter.ViewListener;

public class ConditionEditorDialog extends AbstractEditorDialog<Condition> {

    private ViewListener.ConditionViewListener presenter;
    private Runnable run;

    public ConditionEditorDialog(String title, Condition condition, ViewListener.ConditionViewListener presenter, Runnable run) {
        super(title, condition);
        this.run = run;
        this.presenter = presenter;
        this.init(title);
    }

    protected void fetch() {
        load();
        super.fetch();
    }

    @Override
    protected Component buildFormContent() {
        FormLayout layout = new FormLayout();

        TextField description = new TextField();
        binder.forField(description).bind(Condition::getName, Condition::setName);
        layout.addFormItem(description, "Name");

        TextField prescription = new TextField();
        binder.forField(prescription).bind(Condition::getSymptoms, Condition::setSymptoms);
        layout.addFormItem(prescription, "Symptoms");

        TextField type = new TextField();
        binder.forField(type).bind(Condition::getRecommendedTreatment, Condition::setRecommendedTreatment);
        layout.addFormItem(type, "Recommended Treatment");


        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );
        return layout;
    }

    private void load() {
    }

    @Override
    protected void save(ClickEvent<Button> e) {
        try {
            presenter.save(binder.getBean());
        } catch (ConcurrencyException ce) {
            Notification.show("Document was updated by another user", 5000, Notification.Position.TOP_CENTER);
        }
        run.run();
        this.close();
    }

}

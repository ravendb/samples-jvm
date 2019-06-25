package net.ravendb.demo.view.editor;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import net.ravendb.client.exceptions.ConcurrencyException;
import net.ravendb.demo.model.Doctor;
import net.ravendb.demo.presenter.ViewListener;

public class DoctorEditorDialog extends AbstractEditorDialog<Doctor> {

    private ViewListener.DoctorViewListener presenter;
    private Runnable run;
    ComboBox<String> department;

    public DoctorEditorDialog(String title, Doctor bean, ViewListener.DoctorViewListener presenter, Runnable run) {
        super(title, bean);
        this.run = run;
        this.presenter = presenter;
        this.init(title);
    }

    protected void fetch() {
        department.setItems(presenter.getDepartments());
        super.fetch();
    }

    @Override
    protected Component buildFormContent() {
        FormLayout layout = new FormLayout();

        TextField name = new TextField();
        name.setRequiredIndicatorVisible(true);
        binder.forField(name)
              .asRequired()
              .bind(Doctor::getName, Doctor::setName);

        layout.addFormItem(name, "Name");

        TextField age = new TextField();
        binder.forField(age)
              .withConverter(new StringToIntegerConverter(""))
              .asRequired()
              .bind(Doctor::getAge, Doctor::setAge);

        layout.addFormItem(age, "Age");

        department = new ComboBox<>();

        binder.forField(department).bind(Doctor::getDepartment, Doctor::setDepartment);
        layout.addFormItem(department, "Department");

        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return layout;
    }

    @Override
    protected void save(ClickEvent<Button> e) {
        try {
            presenter.save(binder.getBean());
            run.run();
        } catch (ConcurrencyException ce) {
            Notification.show("Document was updated by another user",
                              5000, Notification.Position.TOP_CENTER);
        }

        this.close();
    }

}

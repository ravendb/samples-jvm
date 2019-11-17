package net.ravendb.demo.view.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.ravendb.demo.model.DTO.ProfilePicture;
import net.ravendb.demo.model.Patient;
import net.ravendb.demo.presenter.ViewListener;
import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import net.ravendb.client.exceptions.ConcurrencyException;
import net.ravendb.demo.model.asset.Gender;

public class PatientEditorDialog extends AbstractEditorDialog<Patient> {

    private static Logger logger = Logger.getLogger(PatientEditorDialog.class.getSimpleName());

    private final String ATTACHMENT_NAME = "profile_picture";

    private ViewListener.PatientViewListener presenter;
    private Image image;
    private ProfilePicture profilePicture;
    private Runnable run;

    public PatientEditorDialog(String title, Patient bean,
                               ViewListener.PatientViewListener presenter, Runnable run) {
        super(title, bean);
        this.run = run;
        this.presenter = presenter;
        image = new Image("/frontend/images/avatar.jpeg", "");
        
        if (bean.getId() != null) {
            profilePicture = presenter.getProfilePicture(bean);
            
            if (profilePicture != null) {
                image = new Image(profilePicture.getStreamResource(), "");
            }
        }
        
        this.init(title);
    }

    @Override
    protected Component buildFormContent() {
        FormLayout layout = new FormLayout();
        HorizontalLayout photoLayout = new HorizontalLayout();        

        image.setWidth("60px");
        image.setHeight("60px");
        image.getStyle().set("borderRadius", "50%");
        
        photoLayout.add(image);

        MemoryBuffer fileBuffer = new MemoryBuffer();
        Upload upload = new Upload(fileBuffer);

        upload.addSucceededListener(e -> {
            this.processUpload(e, fileBuffer);
        });
        upload.setDropAllowed(false);

        photoLayout.add(upload);

        layout.add(photoLayout);

        TextField firstname = new TextField();
        firstname.setRequiredIndicatorVisible(true);
        binder.forField(firstname)
              .asRequired()
              .bind(p -> p.getFirstName(),
                    (p, s) -> p.setFirstName(s));
        layout.addFormItem(firstname, "First Name");

        TextField lastname = new TextField();
        lastname.setRequiredIndicatorVisible(true);
        binder.forField(lastname)
              .asRequired()
              .bind(p -> p.getLastName(),
                    (p, s) -> p.setLastName(s));
        layout.addFormItem(lastname, "Last Name");

        TextField email = new TextField();
        lastname.setRequiredIndicatorVisible(true);
        binder.forField(email)
              .asRequired()
              .bind(p -> p.getEmail(),
                    (p, s) -> p.setEmail(s));
        layout.addFormItem(email, "Email");

        ComboBox<Gender> gender = new ComboBox<>();
        gender.setItems(Gender.values());
        binder.forField(gender).bind(p -> p.getGender(),
                                     (p, s) -> p.setGender(s));
        layout.addFormItem(gender, "Gender");

        DatePicker birth = new DatePicker();
        birth.setWeekNumbersVisible(false);
        binder.forField(birth).bind(p -> p.getBirthLocalDate(),
                                    (p, s) -> p.setBirthLocalDate(s));
        layout.addFormItem(birth, "Date of birth");

        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1)
        );

        return layout;
    }

    private void processUpload(SucceededEvent event, MemoryBuffer fileBuffer) {
        InputStream is = fileBuffer.getInputStream();

        try {
            byte[] bytes = IOUtils.toByteArray(is);
            image.getElement().setAttribute("src", new StreamResource(
                    ATTACHMENT_NAME, () -> new ByteArrayInputStream(bytes)));

            
            if (profilePicture == null)
                profilePicture = new ProfilePicture();
            
            //create profilePicture
            profilePicture.setBytes(bytes);
            profilePicture.setName(ATTACHMENT_NAME);

            try (ImageInputStream in = ImageIO.createImageInputStream(
                                       new ByteArrayInputStream(bytes))) {
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);

                if (readers.hasNext()) {
                    ImageReader reader = readers.next();

                    try {
                        reader.setInput(in);
                        image.setWidth("60px");
                        image.setHeight("60px");
                    } finally {
                        reader.dispose();
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    @Override
    protected void save(ClickEvent<Button> e) {
        try {
            if (binder.getBean().getId() != null)
                presenter.update(binder.getBean(), profilePicture);
            else
                presenter.create(binder.getBean(), profilePicture);

        } catch (ConcurrencyException ce) {
            Notification.show("Document was updated by another user_",
                              5000, Notification.Position.TOP_CENTER);
            System.out.println(ce);
        }

        run.run();
        this.close();
    }

}

package net.ravendb.demo.view;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.vaadin.flow.component.notification.Notification;
import net.ravendb.client.exceptions.ConcurrencyException;
import net.ravendb.demo.model.DTO.ProfilePicture;
import net.ravendb.demo.model.Patient;
import net.ravendb.demo.presenter.ViewListener;
import org.apache.commons.lang3.tuple.Pair;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import net.ravendb.demo.model.asset.Gender;
import net.ravendb.demo.view.editor.AddressEditorDialog;
import net.ravendb.demo.view.editor.PatientEditorDialog;
import net.ravendb.demo.view.grid.PageableGrid;
import net.ravendb.demo.presenter.PatientPresenter;

@Route(value = "patient", layout = RavenDBApp.class)
@PageTitle(value = "Hospital Management")
public class PatientView extends VerticalLayout {
    private static Logger logger = Logger.getLogger(PatientView.class.getSimpleName());
    
    private final int PAGE_SIZE = 10;
    private final ViewListener.PatientViewListener presenter;
    private PageableGrid<Patient> grid;
    private Button edit, delete, visits;
    private Checkbox order;
    private TextField search;

    public PatientView() {
        presenter = new PatientPresenter();
        init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        presenter.openSession();
        loadPage();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        presenter.releaseSession();
        super.onDetach(detachEvent);
    }

    private void init() {
        this.setWidth("100%");
        H4 title = new H4("Patients");
        add(title);

        add(createHeader());
        add(createSearchBox());
        add(createGrid());
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();

        Button add = new Button("Add", e -> {
            PatientEditorDialog d = new PatientEditorDialog("Add",
                    new Patient(), this.presenter, () -> {
                loadPage();
            });

            d.open();
        });

        header.add(add);

        edit = new Button("Edit", e -> {
            Patient patient = this.grid.getGrid().asSingleSelect().getValue();            
            
            PatientEditorDialog d = new PatientEditorDialog("Edit",
                    patient, this.presenter, () -> {
                loadPage();
            });

            d.open();
        });

        edit.setEnabled(false);
        header.add(edit);

        delete = new Button("Delete", e -> {
            ConfirmDialog.createQuestion().withCaption("System alert").withMessage("Do you want to delete?")
                    .withOkButton(() -> {
                Patient patient = grid.getGrid().asSingleSelect().getValue();
                try {
                    presenter.delete(patient);
                } catch (ConcurrencyException ce) {
                    Notification.show("Document was updated by another user",
                                      5000, Notification.Position.TOP_CENTER);
                    System.out.println(ce);
                }
                
                loadPage();
                
            }, ButtonOption.focus(), ButtonOption.caption("YES"))
                                    .withCancelButton(ButtonOption.caption("NO"))
                                    .open();
        });

        delete.setEnabled(false);
        header.add(delete);

        visits = new Button("Manage Visits", e -> {
            Map<String, String> map = new HashMap<>();
            map.put("patientId", grid.getGrid().asSingleSelect().getValue().getId());

            UI.getCurrent().navigate("patient/patientvisit/"
                    + Base64.getEncoder().encodeToString((grid.getGrid()
                    .asSingleSelect().getValue().getId()).getBytes()));
        });

        visits.setEnabled(false);
        header.add(visits);

        return header;
    }

    private Component createSearchBox() {
        HorizontalLayout layout = new HorizontalLayout();
        Span span = new Span();

        search = new TextField();
        search.setPlaceholder("Search");
        search.addKeyDownListener(com.vaadin.flow.component.Key.ENTER,
                (ComponentEventListener<KeyDownEvent>) keyDownEvent -> {
                    loadPage();
                });

        order = new Checkbox("Order by birth date");
        order.addValueChangeListener(e -> {
            loadPage();
        });

        span.add(new Icon(VaadinIcon.SEARCH), search, order);

        layout.add(span);
        return layout;
    }

    private Component createGrid() {
        grid = new PageableGrid<Patient>(this::loadPage);
        grid.getGrid().setSelectionMode(SelectionMode.SINGLE);
        grid.setWidth("100%");

        grid.getGrid().addComponentColumn(p -> {
            ProfilePicture profilePicture = presenter.getProfilePicture(p);
            Image image = new Image("/frontend/images/avatar.jpeg", "");
            
            if (profilePicture != null) {
                image = new Image(profilePicture.getStreamResource(), "");
            }
            
            image.setWidth("60px");
            image.setHeight("60px");
            image.getStyle().set("borderRadius", "50%");
            return image;
        });

        grid.getGrid().addColumn(p -> p.getFirstName()).setHeader("First Name");
        grid.getGrid().addColumn(p -> p.getLastName()).setHeader("Last Name");
        grid.getGrid().addColumn(p -> p.getEmail()).setHeader("Email");

        grid.getGrid().addColumn(new ComponentRenderer<>(p -> {
            if (p.getGender() == Gender.MALE) {
                return new Icon(VaadinIcon.MALE);
            } else {
                return new Icon(VaadinIcon.FEMALE);
            }
        })).setHeader("Gender");

        grid.getGrid().addColumn(new LocalDateRenderer<>(p -> p.getBirthLocalDate(),
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))).setHeader("Birth Date");

        grid.getGrid().addComponentColumn(p -> {
            Button address = new Button();
            address.setIcon(new Icon(VaadinIcon.HOME));
            address.addClickListener(e -> {
                AddressEditorDialog d = new AddressEditorDialog("Address",
                        p, this.presenter);
                d.open();
            });

            return address;
        }).setHeader("Address");

        grid.getGrid().addSelectionListener(e -> {
            if (grid.getGrid().getSelectedItems().size() > 0) {
                edit.setEnabled(true);
                delete.setEnabled(true);
                visits.setEnabled(true);
            } else {
                edit.setEnabled(false);
                delete.setEnabled(false);
                visits.setEnabled(false);
            }
        });

        return grid;
    }

    Collection<Patient> currentPage = null;
    
    private void loadPage() {
        int page = grid.getPaginator().getPage();
        Pair<Collection<Patient>, Integer> results;
        
        if (search.getValue().length() > 1) {
            results =  presenter.searchPatientsList(page * PAGE_SIZE, PAGE_SIZE, search.getValue(), order.getValue());
            grid.getPaginator().setPage(0);
        } else {
            results =  presenter.getPatientsList(page * PAGE_SIZE, PAGE_SIZE, order.getValue());
        }
        
        currentPage = results.getLeft();
        grid.getGrid().setItems(results.getLeft());
        grid.getPaginator().setTotal(results.getRight());
    }

}

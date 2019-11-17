package net.ravendb.demo.view;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;

import net.ravendb.demo.model.Visit;
import net.ravendb.demo.presenter.ViewListener;
import org.apache.commons.lang3.tuple.Pair;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import net.ravendb.demo.model.DTO.PatientVisit;
import net.ravendb.demo.view.grid.PageableGrid;
import net.ravendb.demo.presenter.VisitsPresenter;

@Route(value = "visits", layout = RavenDBApp.class)
@PageTitle(value = "Hospital Management")
public class VisitsView extends VerticalLayout {
    private final int PAGE_SIZE = 10;

    private H5 name;
    private ViewListener.VisitsViewListener presenter;
    private PageableGrid<PatientVisit> grid;
    private Checkbox order;
    private TextField search;

    public VisitsView() {
        presenter = new VisitsPresenter();
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
        H4 title = new H4("Visits");
        add(title);

        name = new H5();
        name.setClassName("name");
        add(name);

        add(createHeader());
        add(createSearchBox());
        add(createGrid());
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

        order = new Checkbox("Order by visit date");
        order.addValueChangeListener(e -> {
            loadPage();
        });

        span.add(new Icon(VaadinIcon.SEARCH), search, order);

        layout.add(span);
        return layout;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();

        return header;
    }

    private Component createGrid() {
        grid = new PageableGrid<>(this::loadPage);
        grid.getGrid().setSelectionMode(SelectionMode.SINGLE);
        grid.setWidth("100%");

        grid.getGrid().addColumn(v -> v.getDoctorName()).setHeader("Doctor");
        grid.getGrid().addColumn(new LocalDateRenderer<>(PatientVisit::getLocalDate,
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))).setHeader("Visit Date");

        grid.getGrid().addColumn(v -> v.getFirstName()).setHeader("First Name");
        grid.getGrid().addColumn(v -> v.getLastName()).setHeader("Last Name");
        grid.getGrid().addColumn(v -> v.getVisitSummary()).setHeader("Visit Summary");
        return grid;
    }

    private void loadPage() {
        int page = grid.getPaginator().getPage();
        Pair<Collection<PatientVisit>, Integer> results;
        
        if (search.getValue().length() > 1) {
            results = presenter.searchVisitsList(page * PAGE_SIZE, PAGE_SIZE, search.getValue(), order.getValue());
        } else {
            results = presenter.getVisitsList(page * PAGE_SIZE, PAGE_SIZE, order.getValue());
        }
        
        grid.getGrid().setItems(results.getLeft());
        grid.getPaginator().setTotal(results.getRight());
    }

}
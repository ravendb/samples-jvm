package net.ravendb.demo.view.grid;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import com.nega.NegaPaginator;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class PageableGrid<T> extends VerticalLayout {
    private static final int PAGE_SIZE = 10;

    @FunctionalInterface
    public interface PageableCallback<T> {
        void loadPage();
    }

    private NegaPaginator paginator = new NegaPaginator();
    private Grid<T> grid = new Grid<>();
    private final PageableCallback pageableCallback;

    public PageableGrid(PageableCallback pageableCallback) {
        paginator.setInitialPage(false);
        paginator.setPage(0);
        paginator.setSize(PAGE_SIZE);
        this.pageableCallback = pageableCallback;
        buildUI();
    }

    private void buildUI() {
        setSizeFull();

        paginator.setInitialPage(true);

        paginator.addPageChangeListener(e -> {
            pageableCallback.loadPage();
        });

        add(grid, paginator);
        setAlignItems(Alignment.CENTER);
    }

    public Grid<T> getGrid() {
        return this.grid;
    }

    public NegaPaginator getPaginator() {
        return this.paginator;
    }

}

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
//    private int size;

    public PageableGrid(PageableCallback pageableCallback) {
//        this.size = PAGE_SIZE;
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

//    private void onPageChange() {
//        /*Pair<Collection<T>, Integer> result = */
////        grid.setItems(result.getKey());
////        paginator.setTotal(result.getValue());
//    }    

//    public void loadFirstPage() {
//        /*Pair<Collection<T>, Integer> result = */pageableCallback.loadPage(0, PAGE_SIZE);
//        
//        grid.setItems(result.getKey());
//        paginator.setPage(0);
//        paginator.setTotal(result.getValue());
//    }

    public Grid<T> getGrid() {
        return this.grid;
    }

    public NegaPaginator getPaginator() {
        return this.paginator;
    }

}

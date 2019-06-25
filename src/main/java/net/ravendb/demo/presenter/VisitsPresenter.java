package net.ravendb.demo.presenter;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.ravendb.client.documents.session.IDocumentQuery;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.documents.session.QueryStatistics;
import net.ravendb.client.primitives.Reference;
import net.ravendb.demo.model.DTO.PatientVisit;
import net.ravendb.demo.model.Patient;

public class VisitsPresenter implements ViewListener.VisitsViewListener {

    private IDocumentSession session;

    public VisitsPresenter() {}

    @Override
    public Pair<Collection<PatientVisit>, Integer> getVisitsList(int offset, int limit, boolean order) {
        Reference<QueryStatistics> statsRef = new Reference<>();
        IDocumentQuery<PatientVisit> visits = session.query(Patient.class)
                .groupBy("visits[].doctorName", "visits[].date", "firstName", "lastName", "visits[].visitSummary")
                .selectKey("visits[].doctorName", "doctorName").selectKey("visits[].date", "date")
                .selectKey("visits[].visitSummary", "visitSummary").selectKey("firstName", "firstName")
                .selectKey("lastName", "lastName")
                .selectCount()
                .ofType(PatientVisit.class)
                .skip(offset)
                .take(limit)
                .statistics(statsRef);

        if (order) {
            visits.orderByDescending("date");
        } else {
            visits.orderBy("date");
        }

        List<PatientVisit> list = visits.toList();

        int totalResults = statsRef.value.getTotalResults();

        return new ImmutablePair<Collection<PatientVisit>, Integer>(list, totalResults);
    }


    @Override
    public Pair<Collection<PatientVisit>, Integer> searchVisitsList(int offset, int limit, String term, boolean order) {
        Reference<QueryStatistics> statsRef = new Reference<>();
        IDocumentQuery<PatientVisit> visits = session.advanced().documentQuery(Patient.class)
                .groupBy("visits[].doctorName", "visits[].date", "firstName", "lastName", "visits[].visitSummary")
                .selectKey("visits[].doctorName", "doctorName").selectKey("visits[].date", "date")
                .selectKey("visits[].visitSummary", "visitSummary").selectKey("firstName", "firstName")
                .selectKey("lastName", "lastName")
                .selectCount()
                .ofType(PatientVisit.class)
                .whereStartsWith("doctorName", term)
                .skip(offset)
                .take(limit)
                .statistics(statsRef);

        if (order) {
            visits.orderByDescending("date");
        } else {
            visits.orderBy("date");
        }

        List<PatientVisit> list = visits.toList();
        int totalResults = statsRef.value.getTotalResults();

        return new ImmutablePair<Collection<PatientVisit>, Integer>(list, totalResults);
    }

    @Override
    public void openSession() {

        if (session == null) {
            session = RavenDBDocumentStore.getStore().openSession();
        }
    }

    @Override
    public void releaseSession() {
        session.close();
    }
}

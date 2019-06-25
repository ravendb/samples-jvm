package net.ravendb.demo.presenter;

import java.util.Collection;
import java.util.Collections;

import net.ravendb.client.documents.session.IDocumentQuery;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.demo.model.DTO.PatientVisit;
import net.ravendb.demo.model.Condition;
import net.ravendb.demo.model.asset.Configuration;
import net.ravendb.demo.model.Doctor;
import net.ravendb.demo.model.Patient;
import net.ravendb.demo.model.Visit;

public class PatientVisitPresenter implements ViewListener.PatientVisitViewListener {

    private IDocumentSession session;

    public PatientVisitPresenter() {}

    @Override
    public Collection<PatientVisit> getVisitsList(String patientId, String term, boolean order) {
        Patient patient = session.load(Patient.class, patientId);

        IDocumentQuery<PatientVisit> visits = session.query(Patient.class)
                .waitForNonStaleResults()
                .groupBy("visits[].doctorName", "visits[].date", "visits[].type", "visits[].conditionId", "firstName", "lastName", "visits[].visitSummary")
                .selectKey("visits[].doctorName", "doctorName")
                .selectKey("visits[].date", "date")
                .selectKey("visits[].visitSummary", "visitSummary")
                .selectKey("firstName", "firstName")
                .selectKey("lastName", "lastName")
                .selectKey("visits[].type", "type")
                .selectKey("visits[].conditionId", "conditionId")
                .selectCount()
                .ofType(PatientVisit.class)
                .whereEquals("firstName", patient.getFirstName())
                .whereEquals("lastName", patient.getLastName());

        if (term != null) {
            visits.whereStartsWith("doctorName", term);
        }

        if (order) {
            return visits.orderByDescending("date").toList();
        } else {
            return visits.orderBy("date").toList();
        }
    }

    @Override
    public void save(String patientId, Visit visit) {
        Patient patient = session.load(Patient.class, patientId);
        patient.getVisits().add(visit);
        session.store(patient);
        session.saveChanges();
    }

    @Override
    public Patient getPatientById(String id) {
        Patient patient = session.load(Patient.class, id);
        return patient;
    }

    @Override
    public Collection<Doctor> getDoctorsList() {
        return session.query(Doctor.class).distinct().toList();
    }

    @Override
    public Collection<Condition> getConditionsList() {

        return session.query(Condition.class).distinct().toList();

    }

    @Override
    public Condition getConditionById(String conditionId) {

        return session.load(Condition.class, conditionId);

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

    @Override
    public Collection<String> getLocationsList() {
        Configuration configuration = session.load(Configuration.class, "configurations/options");
        if (configuration != null) {
            return configuration.getLocations();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

}

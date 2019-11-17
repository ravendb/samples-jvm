package net.ravendb.demo.presenter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.demo.model.DTO.DoctorVisit;
import net.ravendb.demo.model.asset.Configuration;
import net.ravendb.demo.model.Doctor;
import net.ravendb.demo.model.Patient;

public class DoctorPresenter implements ViewListener.DoctorViewListener {

    private IDocumentSession session;

    public DoctorPresenter() {}

    @Override
    public Collection<Doctor> getDoctorsList() {
        return session.query(Doctor.class).toList();
    }

    @Override
    public Collection<String> getDepartments() {
        Configuration configuration = session.load(Configuration.class, "configurations/options");

        if (configuration != null) {
            return configuration.getDepartments();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void save(Doctor doctor) {
        session.store(doctor);
        session.saveChanges();

    }

    @Override
    public void delete(Doctor doctor) {
        session.delete(doctor);
        session.saveChanges();
    }

    @Override
    public Collection<DoctorVisit> getDoctorVisitsList() {
        List<DoctorVisit> results = session.query(Patient.class)
                                           .groupBy("visits[].doctorId","visits[].doctorName")
                                           .selectKey("visits[].doctorId", "doctorId")
                                           .selectKey("visits[].doctorName", "doctorName") 
                                           .selectCount()
                                           .whereNotEquals("doctorId", null)
                                           .orderByDescending("count")
                                           .ofType(DoctorVisit.class)
                                           .include("visits[].doctorId")
                                           .toList();

        // fetch doctors by batch
        Set<String> doctorIds = results.stream().map(p -> p.getDoctorId()).collect(Collectors.toSet());
        Map<String, Doctor> map = session.load(Doctor.class, doctorIds);

        results.forEach(r -> {
            Doctor doctor = map.get(r.getDoctorId());
            if (doctor != null)
                r.setDoctorName(doctor.getName());
            else
                r.setDoctorName(r.getDoctorName() + " (deleted)");
        });

        assert (session.advanced().getNumberOfRequests() == 1);
        return results;

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

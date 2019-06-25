package net.ravendb.demo.presenter;

import net.ravendb.demo.model.DTO.DoctorVisit;
import net.ravendb.demo.model.DTO.PatientVisit;
import net.ravendb.demo.model.*;
import net.ravendb.demo.model.DTO.ProfilePicture;
import net.ravendb.demo.model.asset.Address;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public interface ViewListener {

    interface BaseViewListener {    }
    
    interface ConditionViewListener extends BaseViewListener {

        Condition getConditionById(String id);

        Patient getPatientById(String id);

        void save(Condition condition);

        void delete(Condition condition);

        Pair<Collection<Condition>, Integer> getConditionsList(int offset, int limit, String term);

        void openSession();

        void releaseSession();
    }

    interface DoctorViewListener extends BaseViewListener {

        Collection<Doctor> getDoctorsList();

        Collection<String> getDepartments();

        void save(Doctor doctor);

        void delete(Doctor doctor);

        Collection<DoctorVisit> getDoctorVisitsList();

        void openSession();

        void releaseSession();
    }

    interface PatientViewListener extends BaseViewListener {

        Pair<Collection<Patient>, Integer> getPatientsList(int offset, int limit, boolean order);

        Collection<String> getRegionsList();

        void create(Patient patient, ProfilePicture profilePicture);

        void update(Patient patient, ProfilePicture profilePicture);

        void saveAddress(String patientId, Address address);

        void delete(Patient patient);

        Pair<Collection<Patient>, Integer> searchPatientsList(int offset, int limit, String term, boolean order);

        void openSession();

        void releaseSession();

        ProfilePicture getProfilePicture(Patient patient);
    }

    interface PatientVisitViewListener extends BaseViewListener {

        Collection<PatientVisit> getVisitsList(String patientId, String term, boolean order);

        Condition getConditionById(String conditionId);

        void save(String patientId, Visit visit);

        Patient getPatientById(String id);

        Collection<Doctor> getDoctorsList();

        Collection<Condition> getConditionsList();

        Collection<String> getLocationsList();

        void openSession();

        void releaseSession();
    }

    interface VisitsViewListener extends BaseViewListener {

        Pair<Collection<PatientVisit>, Integer> getVisitsList(int offset, int limit, boolean order);

        Pair<Collection<PatientVisit>, Integer> searchVisitsList(int offset, int limit, String term, boolean order);

        void openSession();

        void releaseSession();
    }
}

package net.ravendb.demo.presenter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.flow.server.VaadinServletRequest;
import net.ravendb.demo.model.DTO.ProfilePicture;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.ravendb.client.documents.operations.attachments.AttachmentName;
import net.ravendb.client.documents.operations.attachments.CloseableAttachmentResult;
import net.ravendb.client.documents.session.IDocumentQuery;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.documents.session.QueryStatistics;
import net.ravendb.client.exceptions.ConcurrencyException;
import net.ravendb.client.primitives.Reference;
import net.ravendb.demo.model.asset.Address;
import net.ravendb.demo.model.asset.Configuration;
import net.ravendb.demo.model.Patient;

public class PatientPresenter implements ViewListener.PatientViewListener {
    private static Logger logger = Logger.getLogger(PatientPresenter.class.getSimpleName());
    private final String ATTACHMENT_NAME = "profile_picture";

    private IDocumentSession session;

    public PatientPresenter() {}

    @Override
    public Pair<Collection<Patient>, Integer> getPatientsList(int offset, int limit, boolean order) {
        Reference<QueryStatistics> statsRef = new Reference<>();
        IDocumentQuery<Patient> query = session.query(Patient.class)
                .skip(offset)
                .take(limit)
                .statistics(statsRef);

        if (order) {
            query.orderBy("birthDate");
        }

        Collection<Patient> patients = query.toList();
        int totalResults = statsRef.value.getTotalResults();

        return new ImmutablePair<Collection<Patient>, Integer>(patients, totalResults);
    }

    @Override
    public Pair<Collection<Patient>, Integer> searchPatientsList(
           int offset, int limit, String term, boolean order) {
        Reference<QueryStatistics> statsRef = new Reference<>();
        IDocumentQuery<Patient> query = session.query(Patient.class)
                .whereStartsWith("firstName", term)
                .skip(offset)
                .take(limit)
                .statistics(statsRef);

        if (order) {
            query.orderBy("birthDate");
        }

        Collection<Patient> patients = query.toList();
        int totalResults = statsRef.value.getTotalResults();

        return new ImmutablePair<Collection<Patient>, Integer>(patients, totalResults);
    }

    @Override
    public Collection<String> getRegionsList() {
        Configuration configuration = session.load(Configuration.class, "configurations/options");

        if (configuration != null) {
            return configuration.getRegions();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void create(Patient patient, ProfilePicture profilePicture) {

        session.store(patient);

        if (profilePicture != null) {
            session.advanced().attachments().store(patient, ATTACHMENT_NAME,
                    profilePicture.getInputStream());
        }

        session.saveChanges();
    }

    @Override
    public void update(Patient patient, ProfilePicture profilePicture) throws ConcurrencyException {
        session.store(patient);
        
        if (profilePicture != null) {
            session.advanced().attachments().store(patient.getId(), ATTACHMENT_NAME,
                    profilePicture.getInputStream());
        }     
        
        session.saveChanges();
    }

    @Override
    public void saveAddress(String patientId, Address address) {
        Patient patient = session.load(Patient.class, patientId);
        patient.setAddress(address);
        session.store(patient);
        session.saveChanges();
    }

    @Override
    public void delete(Patient patient) {
        session.delete(patient);
        session.saveChanges();
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
    public ProfilePicture getProfilePicture(Patient patient) {
        ProfilePicture profilePicture = new ProfilePicture();

            try (CloseableAttachmentResult result = session.advanced()
                    .attachments()
                    .get(patient.getId(), ATTACHMENT_NAME)) {


                if (result == null)
                    return null;
                InputStream data = result.getData();
                byte[] bytes = IOUtils.toByteArray(data);
                profilePicture.setBytes(bytes);
                profilePicture.setName(ATTACHMENT_NAME);
                
                return profilePicture;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "", e);
            }
        
        return null;
    }

}

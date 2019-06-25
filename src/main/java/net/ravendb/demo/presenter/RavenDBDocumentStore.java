package net.ravendb.demo.presenter;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.conventions.DocumentConventions;

public final class RavenDBDocumentStore {

    private static IDocumentStore store;

    static {

        store = new DocumentStore(new String[]{"http://127.0.0.1:8080"},
                                  "Hospital");


        DocumentConventions conventions = store.getConventions();
        conventions.setUseOptimisticConcurrency(true);

        store.initialize();
    }

    public static IDocumentStore getStore() {
        return store;
    }

}

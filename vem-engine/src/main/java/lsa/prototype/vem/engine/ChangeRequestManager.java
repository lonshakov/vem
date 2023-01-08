package lsa.prototype.vem.engine;

import jakarta.persistence.metamodel.PluralAttribute;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.EntityPersister;

//TODO
public class ChangeRequestManager {
    private final SessionImpl session;
    private final Class<?> rootType;
    private final Class<?> requestType;
    private final Class<?> unitType;

    public ChangeRequestManager(SessionImpl session, Class<?> rootType, Class<?> requestType, Class<?> unitType) {
        this.session = session;
        this.rootType = rootType;
        this.requestType = requestType;
        this.unitType = unitType;
    }

    <T extends Root, R extends ChangeRequest<T>> R persist(T root) {
        EntityPersister rootPersister = session.getMetamodel().entityPersister(rootType);
        EntityPersister requestPersister = session.getMetamodel().entityPersister(requestType);
        EntityPersister unitPersister = session.getMetamodel().entityPersister(unitType);
        R request = (R) requestPersister.getEntityTuplizer().instantiate();
        session.persist(request);
        session.persist(root);
        request.setRoot(root);


        for (PluralAttribute attribute : session.getMetamodel().entity(rootType).getPluralAttributes()) {
            for (Leaf leaf : (Iterable<Leaf>) rootPersister.getPropertyValue(root, attribute.getName())) {
                leaf.setParent(root);
                session.persist(leaf);
                ChangeUnit unit = (ChangeUnit) unitPersister.getEntityTuplizer().instantiate();

            }
        }

        // session.getMetamodel().entity(rootType).

        return request;
    }


}

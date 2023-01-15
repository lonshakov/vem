package vem.context;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.context.ChangeRequest;
import vem.entity.Store;

@Entity
public class StoreChangeRequest extends ChangeRequest<Store> {
}

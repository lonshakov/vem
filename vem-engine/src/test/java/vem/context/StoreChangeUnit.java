package vem.context;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import vem.entity.Store;

@Entity
public class StoreChangeUnit extends ChangeUnit<StoreChangeRequest> {
}

package vem.context;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.context.ChangeUnit;

@Entity
public class StoreChangeUnit extends ChangeUnit<StoreChangeRequest> {
}

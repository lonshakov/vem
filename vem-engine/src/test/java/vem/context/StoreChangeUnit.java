package vem.context;

import jakarta.persistence.Entity;
import vem.basemodel.request.ChangeUnitTemplate;

@Entity
public class StoreChangeUnit extends ChangeUnitTemplate<StoreChangeRequest> {
}

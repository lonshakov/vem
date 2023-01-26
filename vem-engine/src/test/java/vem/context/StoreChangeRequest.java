package vem.context;

import jakarta.persistence.Entity;
import vem.basemodel.request.ChangeRequestTemplate;
import vem.entity.Store;

@Entity
public class StoreChangeRequest extends ChangeRequestTemplate<Store> {
}

package vem.context;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import vem.entity.Store;
import lsa.prototype.vem.model.context.ChangeRequest;

import java.util.HashSet;
import java.util.Set;

@Entity
public class StoreChangeRequest extends ChangeRequest<Store> {
    @OneToMany(mappedBy = "request")
    private Set<StoreChangeUnit> units = new HashSet<>();

    public Set<StoreChangeUnit> getUnits() {
        return units;
    }
}

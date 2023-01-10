package vem.context;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lsa.prototype.vem.model.context.ChangeRequest;
import vem.entity.Store;

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

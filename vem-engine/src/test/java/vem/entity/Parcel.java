package vem.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import vem.context.StoreChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import org.hibernate.annotations.Filter;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Parcel extends Leaf<Store> {
    private String name;
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    @Filter(name = "CurrentVersion")
    private List<Item> items = new ArrayList<>();
    @OneToOne
    private StoreChangeUnit changeUnit;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Item> getItems() {
        return items;
    }
}

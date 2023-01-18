package vem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lsa.prototype.vem.model.version.Leaf;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Parcel extends Leaf<Store> {
    private String name;
    @OneToMany(mappedBy = "parent")
    private List<Item> items = new ArrayList<>();

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

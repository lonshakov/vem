package vem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lsa.prototype.vem.model.version.LeafEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Parcel extends LeafEntity<Store> {
    private String name;
    @OneToMany(mappedBy = "parent")
    private List<Item> items = new ArrayList<>();

    public Parcel(String name) {
        this.name = name;
    }

    public Parcel() {
    }

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

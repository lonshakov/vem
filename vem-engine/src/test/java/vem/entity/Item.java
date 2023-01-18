package vem.entity;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.version.LeafEntity;

@Entity
public class Item extends LeafEntity<Parcel> {
    private String name;

    public Item(String name) {
        this.name = name;
    }

    public Item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

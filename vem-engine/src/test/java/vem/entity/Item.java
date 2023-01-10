package vem.entity;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.version.Leaf;

@Entity
public class Item extends Leaf<Parcel> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

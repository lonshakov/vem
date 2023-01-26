package vem.entity;

import vem.basemodel.version.LeafEntity;

import javax.persistence.Entity;

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

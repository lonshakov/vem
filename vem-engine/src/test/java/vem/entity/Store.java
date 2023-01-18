package vem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lsa.prototype.vem.model.version.RootEntity;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Store extends RootEntity {
    private String name;
    @OneToOne(mappedBy = "parent")
    private StoreBody body;
    @OneToMany(mappedBy = "parent")
    private List<Parcel> parcels = new ArrayList<>();

    public Store(String name) {
        this.name = name;
    }

    public Store() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StoreBody getBody() {
        return body;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }
}
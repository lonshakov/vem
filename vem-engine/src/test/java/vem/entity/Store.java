package vem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lsa.prototype.vem.model.version.Root;
import org.hibernate.annotations.Filter;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Store extends Root {
    private String name;
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    @Filter(name = "CurrentVersion")
    private List<Parcel> parcels = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }
}
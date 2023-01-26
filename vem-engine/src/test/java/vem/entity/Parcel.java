package vem.entity;

import vem.basemodel.version.LeafEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
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

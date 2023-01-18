package vem.entity;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.version.Leaf;

@Entity
public class StoreBody extends Leaf<Store> {
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

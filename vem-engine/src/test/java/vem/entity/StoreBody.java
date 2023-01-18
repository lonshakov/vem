package vem.entity;

import jakarta.persistence.Entity;
import lsa.prototype.vem.model.version.LeafEntity;

@Entity
public class StoreBody extends LeafEntity<Store> {
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

package vem.entity;

import vem.basemodel.version.LeafEntity;

import javax.persistence.Entity;

@Entity
public class StoreBody extends LeafEntity<Store> {
    private String address;

    public StoreBody(String address) {
        this.address = address;
    }

    public StoreBody() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

package vem.context;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import vem.entity.Store;

@Entity
public class StoreChangeUnit extends ChangeUnit<Store> {
    @ManyToOne
    private StoreChangeRequest request;

    @Override
    public ChangeRequest<Store> getRequest() {
        return request;
    }


    @Override
    public void setRequest(ChangeRequest<Store> request) {
        this.request = (StoreChangeRequest) request;
    }
}

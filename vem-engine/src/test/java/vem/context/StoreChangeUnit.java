package vem.context;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import org.hibernate.annotations.Any;
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

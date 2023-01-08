package lsa.prototype.vem.context;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lsa.prototype.vem.entity.Item;
import lsa.prototype.vem.entity.Parcel;
import lsa.prototype.vem.entity.Store;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;

@Entity
public class StoreChangeUnit extends ChangeUnit {
    @ManyToOne
    private StoreChangeRequest request;
    @Any(metaDef = "LeafMapping",
            metaColumn = @Column(name = "leaf_type"))
    @JoinColumn(name = "leaf_id")
    @AnyMetaDef(
            name = "LeafMapping",
            metaType = "string",
            idType = "long",
            metaValues = {
                    @MetaValue(value = "Parcel", targetEntity = Parcel.class),
                    @MetaValue(value = "Store", targetEntity = Store.class),
                    @MetaValue(value = "Item", targetEntity = Item.class)
            }
    )
    private Leaf<?> leaf;

    public void setLeaf(Leaf<?> leaf) {
        this.leaf = leaf;
    }

    @Override
    public Leaf<?> getLeaf() {
        return leaf;
    }

    @Override
    public ChangeRequest<?> getChangeRequest() {
        return request;
    }

    public void setRequest(StoreChangeRequest request) {
        this.request = request;
    }
}

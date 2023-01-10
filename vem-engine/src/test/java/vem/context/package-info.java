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
package vem.context;

import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;
import vem.entity.Item;
import vem.entity.Parcel;
import vem.entity.Store;
package vem.basemodel.version;

import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.Root;

@MappedSuperclass
public class RootEntity extends VersionedEntity implements Root {
}

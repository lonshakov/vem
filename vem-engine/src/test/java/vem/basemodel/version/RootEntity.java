package vem.basemodel.version;

import io.persistence.vem.domain.model.Root;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class RootEntity extends VersionedEntity implements Root {
}

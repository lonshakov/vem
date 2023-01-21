package lsa.prototype.vem.model;

public interface IVersionedEntity extends IGlobalEntity {
    EntityVersion getVersion();

    void setVersion(EntityVersion version);

    void setVersion(EntityVersion.StateType stateType, long date);
}

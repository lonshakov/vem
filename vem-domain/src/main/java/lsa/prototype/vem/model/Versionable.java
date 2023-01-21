package lsa.prototype.vem.model;

public interface Versionable extends GlobalEntity {
    Version getVersion();

    void setVersion(Version version);

    void setVersion(Version.StateType stateType, long date);
}

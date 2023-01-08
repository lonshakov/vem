package lsa.prototype.vem.model.basic;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Dictionary extends PersistedObject {
    private String code;

    public String getCode() {
        return code;
    }
}

package vem.basemodel.basic;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Dictionary extends PersistedObject {
    private String code;

    public String getCode() {
        return code;
    }
}

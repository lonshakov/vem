package vem.basemodel.basic;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Dictionary extends PersistedObject {
    private String code;

    public String getCode() {
        return code;
    }
}

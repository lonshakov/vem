package lsa.prototype.vem.model.context;

import jakarta.persistence.Embeddable;

@Embeddable
public class Sign {
    private String user;
    private long date;

    public Sign() {
    }

    public Sign(String user, long date) {
        this.user = user;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public long getDate() {
        return date;
    }
}

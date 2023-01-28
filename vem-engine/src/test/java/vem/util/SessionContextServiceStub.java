package vem.util;

import io.persistence.vem.spi.context.SessionContext;
import io.persistence.vem.spi.context.SessionContextService;

public class SessionContextServiceStub implements SessionContextService {
    @Override
    public SessionContext getUserContext() {
        return () -> () -> "dummy";
    }
}

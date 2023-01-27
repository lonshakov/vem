package vem.util;

import io.persistence.vem.spi.context.SessionContext;
import io.persistence.vem.spi.context.SessionContextService;

public class SessionContextServiceStub implements SessionContextService {
    private final SessionContext context = () -> () -> "dummy";

    @Override
    public SessionContext getUserContext() {
        return context;
    }
}
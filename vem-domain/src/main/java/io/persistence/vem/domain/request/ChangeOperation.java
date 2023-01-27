package io.persistence.vem.domain.request;

public enum ChangeOperation {
    CASCADE_CREATE, CASCADE_DELETE, COLLECTION_ADD, COLLECTION_REMOVE, REFERENCE_REPLACE, REFERENCE_NULLIFY
}

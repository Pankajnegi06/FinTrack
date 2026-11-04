package com.fintrack.storage;

import java.io.IOException;
import java.util.List;

public interface DataStore<T> {
    void saveAll(List<T> items) throws IOException;

    List<T> loadAll() throws IOException;
}

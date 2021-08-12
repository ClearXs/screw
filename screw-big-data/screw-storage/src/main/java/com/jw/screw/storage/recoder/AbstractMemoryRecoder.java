package com.jw.screw.storage.recoder;

import com.jw.screw.storage.properties.StorageProperties;

@Recoder.Callable(name = Recoder.MEMORY)
public abstract class AbstractMemoryRecoder<T> extends AbstractRecoder<T> {

    protected AbstractMemoryRecoder(StorageProperties properties) {
        super(properties);
    }
}

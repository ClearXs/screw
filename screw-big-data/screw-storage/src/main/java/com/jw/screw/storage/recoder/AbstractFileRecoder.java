package com.jw.screw.storage.recoder;

import com.jw.screw.storage.properties.StorageProperties;

@Recoder.Callable(name = Recoder.FILE)
public abstract class AbstractFileRecoder<T> extends AbstractRecoder<T> {

    protected AbstractFileRecoder(StorageProperties properties) {
        super(properties);
    }
}

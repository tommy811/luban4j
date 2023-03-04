package org.luban.monitor;

public interface PropertyListenerSuport {
    void insertPropertyListener(PropertyListener listener);
    void removePropertyListener(PropertyListener listener);
    PropertyListener currentPropertyListener();
}

package org.traincontrol.marklin.network;

import org.traincontrol.model.ModelListener;

public interface NetworkProxy {
    void start();
    void shutdown();
    boolean sendMessage(CS2Message message);
    String getIP();
    void setModel(ModelListener model);
}
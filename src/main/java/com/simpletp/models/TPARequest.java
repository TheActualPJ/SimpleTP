package com.simpletp.models;

import java.util.UUID;

public class TPARequest {

    public enum Type { TPA, TPAHERE }

    private final UUID senderUUID;
    private final UUID targetUUID;
    private final Type type;
    private final long createdAt;

    public TPARequest(UUID senderUUID, UUID targetUUID, Type type) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isExpired(int timeoutSeconds) {
        return (System.currentTimeMillis() - createdAt) >= timeoutSeconds * 1000L;
    }

    public UUID getSenderUUID() { return senderUUID; }
    public UUID getTargetUUID() { return targetUUID; }
    public Type getType() { return type; }
    public long getCreatedAt() { return createdAt; }
}

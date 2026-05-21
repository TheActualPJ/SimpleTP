package com.simpletp.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Home {

    private final UUID ownerUUID;
    private final String name;
    private final String world;
    private final double x, y, z;
    private final float yaw, pitch;

    public Home(UUID ownerUUID, String name, String world, double x, double y, double z, float yaw, float pitch) {
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Home(UUID ownerUUID, String name, Location location) {
        this(ownerUUID, name,
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getName() { return name; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}

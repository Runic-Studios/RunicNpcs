package com.runicrealms.runicnpcs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Npc {

    public EntityPlayer entityPlayer;
    private final GameProfile gameProfile;
    private final Location location;
    private final DataWatcher watcher;
    private final int id;
    private final Hologram hologram;
    private Skin skin;
    private final String uuid;

    private boolean shown;

    public Npc(Location location, Skin skin, Integer id, Hologram hologram, String uuid, boolean shown) {
        this.id = id;
        this.skin = skin;
        this.uuid = uuid;
        gameProfile = new GameProfile(UUID.fromString(uuid), "npc_" + id);
        PropertyMap properties = gameProfile.getProperties();
        if (properties.get("textures").iterator().hasNext()) {
            properties.remove("textures", properties.get("textures").iterator().next());
        }
        properties.put("textures", new Property("textures", this.skin.getTexture(), this.skin.getSignature()));
        this.location = location;
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) this.location.getWorld()).getHandle();
        this.entityPlayer = new EntityPlayer(minecraftServer, worldServer, this.gameProfile, new PlayerInteractManager(worldServer));
        this.entityPlayer.playerConnection = new PlayerConnection(minecraftServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), this.entityPlayer);
        this.entityPlayer.setHealth(1f);
        this.entityPlayer.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(Plugin.getInstance(), true));
        this.entityPlayer.setNoGravity(true);
        this.entityPlayer.setLocation(this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        this.watcher = this.entityPlayer.getDataWatcher();
        this.watcher.set(new DataWatcherObject<Byte>(16, DataWatcherRegistry.a), (byte) 127);
        worldServer.addEntity(this.entityPlayer);
        this.hologram = hologram;
        this.shown = shown;
    }

    public void rotateHeadForPlayer(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(this.entityPlayer, (byte) ((this.location.getYaw() * 256.0F) / 360.0F)));
    }

    public void spawnForPlayer(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.entityPlayer));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.entityPlayer));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.entityPlayer.getId(), this.watcher, true));
        rotateHeadForPlayer(player);
    }

    public void despawnForPlayer(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityPlayer.getId()));
    }

    public void delete() {
        this.hologram.delete();
        this.entityPlayer.die();
    }

    public Integer getEntityId() {
        return this.entityPlayer.getId();
    }

    public int getId() {
        return this.id;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public Location getLocation() {
        return this.location;
    }

    public Skin getSkin() {
        return this.skin;
    }

    public String getUuid() {
        return this.uuid;
    }

    public EntityPlayer getEntityPlayer() {
        return this.entityPlayer;
    }

    public boolean isShown() {
        return this.shown;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
        Plugin.updateNpcs();
    }

}

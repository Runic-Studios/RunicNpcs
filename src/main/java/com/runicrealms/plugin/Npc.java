package com.runicrealms.plugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_16_R3.*;
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

    private final GameProfile gameProfile;
    private final Location location;
    private final DataWatcher watcher;
    private final int id;
    private final Hologram hologram;
    private final UUID uuid;
    public EntityPlayer entityPlayer;
    private Skin skin;
    private Location newLocation = null; // Changes when we move an NPC to be saved on restart

    private boolean shown;

    public Npc(Location location, Skin skin, Integer id, Hologram hologram, UUID uuid, boolean shown) {
        this.id = id;
        this.skin = skin;
        this.uuid = uuid;
        gameProfile = new GameProfile(uuid, "npc_" + id);
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
        this.entityPlayer.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(RunicNpcs.getInstance(), true));
        this.entityPlayer.setNoGravity(true);
        this.entityPlayer.setLocation(this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        this.watcher = this.entityPlayer.getDataWatcher();
        this.watcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 127);
        this.hologram = hologram;
        this.shown = shown;
    }

    public void delete(boolean despawn) {
        this.hologram.delete();
        this.entityPlayer.die();
        if (despawn)
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::despawnForPlayer));
    }

    public void despawnForPlayer(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityPlayer.getId()));
    }

    public Integer getEntityId() {
        return this.entityPlayer.getId();
    }

    public EntityPlayer getEntityPlayer() {
        return this.entityPlayer;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public int getId() {
        return this.id;
    }

    /**
     * @return the label of the Npc "Merchant," "Quest," etc.
     */
    public String getLabel() {
        return this.hologram.getLine(1).toString();
    }

    public void setLabel(String label) {
        this.hologram.getLine(1).removeLine();
        this.hologram.insertTextLine(1, label);
    }

    public Location getLocation() {
        return this.location;
    }

    public String getName() {
        return this.hologram.getLine(0).toString();
    }

    public void setName(String name) {
        this.hologram.getLine(0).removeLine();
        this.hologram.insertTextLine(0, name);
    }

    public Location getNewLocation() {
        return this.newLocation;
    }

    public void setNewLocation(Location newLocation) { // Will only take effect after restart
        this.newLocation = newLocation;
    }

    public Skin getSkin() {
        return this.skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean hasNewLocation() {
        return this.newLocation != null;
    }

    public boolean isShown() {
        return this.shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
        RunicNpcs.updateNpcs();
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

}

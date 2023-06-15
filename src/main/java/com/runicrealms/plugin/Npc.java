package com.runicrealms.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Npc {

    private final WrappedGameProfile gameProfile;
    private final Location location;
    private final WrappedDataWatcher watcher;
    private final int id, entityId;
    private final Hologram hologram;
    private final UUID uuid;
    private Skin skin;
    private Location newLocation = null; // Changes when we move an NPC to be saved on restart

    private boolean shown;

    public Npc(Location location, Skin skin, Integer id, Hologram hologram, UUID uuid, boolean shown) {
        this.location = location;
        this.skin = skin;
        this.id = id;
        this.hologram = hologram;
        this.uuid = uuid;
        this.shown = shown;
        this.entityId = new Random().nextInt(9999 - 1000 + 1) + 1000;

        this.gameProfile = new WrappedGameProfile(uuid, "npc_" + id);
        gameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin.getTexture(), skin.getSignature()));

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(6, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10); // NoGravity flag
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(16, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 127);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(17,WrappedDataWatcher.Registry.get(Boolean.class)), true); // isNpc metadata
        // If the NPC has a custom skin, add it to the data watcher
        //dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(14, ), gameProfile);

        this.watcher = dataWatcher;

    }

    public void delete(boolean despawn) {
        this.hologram.delete();
        if (despawn)
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::despawnForPlayer));
    }

    public void despawnForPlayer(Player player) {
        PacketContainer deathPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        deathPacket.getIntegerArrays().write(0, new int[]{this.getEntityId()});

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, deathPacket);
    }

    public Integer getEntityId() {
        return this.getId();
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
        return this.hologram.getLines().get(1).toString();
    }

    public void setLabel(String label) {
        this.hologram.getLines().remove(1);
        this.hologram.getLines().insertText(1, label);
    }

    public Location getLocation() {
        return this.location;
    }

    public String getName() {
        return this.hologram.getLines().get(0).toString();
    }

    public void setName(String name) {
        this.hologram.getLines().remove(0);
        this.hologram.getLines().insertText(0, name);
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
        PacketContainer rotatePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        rotatePacket.getIntegers().write(0, this.entityId);
        rotatePacket.getBytes().write(0, (byte) ((this.location.getYaw() * 256.05) / 360F));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, rotatePacket);
    }

    public void spawnForPlayer(Player player) {
//        PacketContainer addPlayerPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
//        addPlayerPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
//        addPlayerPacket.getPlayerInfoDataLists().write(0, Collections.singletonList(
//                new PlayerInfoData(this.gameProfile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(this.getName()))
//        ));
//
//        PacketContainer namedEntitySpawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
//        namedEntitySpawnPacket.getIntegers().write(0, this.entityId);
//        namedEntitySpawnPacket.getUUIDs().write(0, this.uuid);
//        namedEntitySpawnPacket.getDoubles().write(0, this.location.getX());
//        namedEntitySpawnPacket.getDoubles().write(1, this.location.getY());
//        namedEntitySpawnPacket.getDoubles().write(2, this.location.getZ());
//        namedEntitySpawnPacket.getBytes().write(0, (byte) (this.location.getYaw() * 256.0F / 360.0F));
//        namedEntitySpawnPacket.getBytes().write(1, (byte) (this.location.getPitch() * 256.0F / 360.0F));
//
//        PacketContainer updateHealthPacket = new PacketContainer(PacketType.Play.Server.UPDATE_HEALTH);
//        updateHealthPacket.getFloat().write(0, 1f);
//
//        PacketContainer entityMetadataPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
//        entityMetadataPacket.getIntegers().write(0, this.entityId);
//        entityMetadataPacket.getWatchableCollectionModifier().write(0, this.watcher.getWatchableObjects());
//
//        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPlayerPacket);
//        ProtocolLibrary.getProtocolManager().sendServerPacket(player, updateHealthPacket);
//        ProtocolLibrary.getProtocolManager().sendServerPacket(player, namedEntitySpawnPacket);
//        ProtocolLibrary.getProtocolManager().sendServerPacket(player, entityMetadataPacket);

        PacketContainer spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        spawnPacket.getIntegers().write(0, this.entityId); // Entity ID
        spawnPacket.getUUIDs().write(0, this.uuid); // Entity UUID
        spawnPacket.getIntegers().write(1, 118); // Entity Type ID (0 for a generic living entity)
        spawnPacket.getDoubles().write(0, this.location.getX());
        spawnPacket.getDoubles().write(1, this.location.getY());
        spawnPacket.getDoubles().write(2, this.location.getZ());
        spawnPacket.getBytes().write(0, (byte) (this.location.getYaw() * 256.0F / 360.0F));
        spawnPacket.getBytes().write(1, (byte) (this.location.getPitch() * 256.0F / 360.0F));
        spawnPacket.getDataWatcherModifier().write(0, this.watcher);

        rotateHeadForPlayer(player);
    }

}
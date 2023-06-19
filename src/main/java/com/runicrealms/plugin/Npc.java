package com.runicrealms.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Npc {

    private final WrappedGameProfile gameProfile;
    private final Location location;
    private final int id;
    private final Hologram hologram;
    private final UUID uuid;
    private Skin skin;
    private Location newLocation = null; // Changes when we move an NPC to be saved on restart

    private boolean shown;

    public Npc(Location location, Skin skin, Integer id, Hologram hologram, UUID uuid, boolean shown) {
        this.id = id;
        this.skin = skin;
        this.uuid = uuid;
        this.gameProfile = new WrappedGameProfile(uuid, "npc_" + id);
        this.location = location;
        this.hologram = hologram;
        this.shown = shown;

        this.gameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", this.skin.getTexture(), this.skin.getSignature()));
    }

    public void delete(boolean despawn) {
        this.hologram.delete();
        if (despawn)
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::despawnForPlayer));
    }

    public void despawnForPlayer(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists().write(0, new ArrayList<>() {{this.add(getEntityId());}});
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public Integer getEntityId() {
        return 9000 + this.getId();
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
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, this.getEntityId());
        packet.getBytes().write(0, (byte) ((this.location.getYaw()  * 256.0F) / 360F));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public void spawnForPlayer(Player player) {
        PacketContainer infoPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        infoPacket.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        PlayerInfoData data = new PlayerInfoData(this.uuid, 0, false, EnumWrappers.NativeGameMode.NOT_SET, this.gameProfile, WrappedChatComponent.fromText(this.getName()));
        infoPacket.getPlayerInfoDataLists().write(1, Collections.singletonList(data));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, infoPacket);

        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getIntegers().write(0, this.getEntityId());
        spawnPacket.getUUIDs().write(0, this.getUuid());
        spawnPacket.getDoubles().write(0, this.location.getX());
        spawnPacket.getDoubles().write(1, this.location.getY());
        spawnPacket.getDoubles().write(2, this.location.getZ());
        spawnPacket.getBytes().write(0, (byte) (this.location.getYaw() * 256.0F / 360.0F));
        spawnPacket.getBytes().write(1, (byte) (this.location.getPitch() * 256.0F / 360.0F));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);

        //this.watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(16, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 127);
        //No Gravity Flag
        //Health: 1F
        //TODO: this.entityPlayer.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(RunicNpcs.getInstance(), true));
        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, this.getEntityId());
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

        rotateHeadForPlayer(player);
    }

}

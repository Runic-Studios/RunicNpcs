package com.runicrealms.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Npc {

    private final WrappedGameProfile gameProfile;
    private final Location location;
    private final WrappedDataWatcher watcher;
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
        this.watcher = new WrappedDataWatcher();
        this.hologram = hologram;
        this.shown = shown;

//        gameProfile = new GameProfile(uuid, "npc_" + id);
//        PropertyMap properties = gameProfile.getProperties();
//        if (properties.get("textures").iterator().hasNext()) {
//            properties.remove("textures", properties.get("textures").iterator().next());
//        }

        gameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", this.skin.getTexture(), this.skin.getSignature()));

//        this.watcher = this.entityPlayer.getDataWatcher();
//        this.watcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 127);
        this.watcher.setObject(16, (byte) 127);
        // No Gravity Flag
        this.watcher.setObject(5, true);
        // TODO: this.entityPlayer.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(RunicNpcs.getInstance(), true));
        // Health: 1F
        this.watcher.setObject(6, 1F);
    }

    public void delete(boolean despawn) {
        this.hologram.delete();
        if (despawn)
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::despawnForPlayer));
    }

    public void despawnForPlayer(Player player) {
      //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityPlayer.getId()));
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, new int[] { this.getEntityId() });
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
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(this.entityPlayer, (byte) ((this.location.getYaw() * 256.0F) / 360.0F)));
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getBytes().write(0, (byte) ((this.location.getYaw()  * 256.0F) / 360F));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public void spawnForPlayer(Player player) {
        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.entityPlayer));
        PacketContainer infoPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        infoPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        infoPacket.getGameProfiles().write(0, this.gameProfile);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, infoPacket);

        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.entityPlayer));
        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getIntegers().write(0, this.getEntityId());
        spawnPacket.getUUIDs().write(0, this.getUuid());
        spawnPacket.getDoubles().write(0, this.location.getX());
        spawnPacket.getDoubles().write(1, this.location.getY());
        spawnPacket.getDoubles().write(2, this.location.getZ());
        spawnPacket.getBytes().write(0, (byte) (this.location.getYaw() * 256.0F / 360.0F));
        spawnPacket.getBytes().write(1, (byte) (this.location.getPitch() * 256.0F / 360.0F));
        spawnPacket.getDataWatcherModifier().write(0, this.watcher);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);

        //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.entityPlayer.getId(), this.watcher, true));
        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, this.getEntityId());
        metadataPacket.getWatchableCollectionModifier().write(0, this.watcher.getWatchableObjects());
        metadataPacket.getBooleans().write(0, true);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

        rotateHeadForPlayer(player);
    }

}

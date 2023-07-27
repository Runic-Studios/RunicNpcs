package com.runicrealms.plugin.npcs;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
    private String name;
    private NpcTag label;
    private HashMap<EnumWrappers.ItemSlot, ItemStack> equipment;

    private boolean shown;

    public Npc(Integer id, Location location, NpcTag label, String name, UUID uuid, Skin skin, Hologram hologram, HashMap<EnumWrappers.ItemSlot, ItemStack> equipment, boolean shown) {
        this.id = id;
        this.location = location;
        this.label = label;
        this.name = name;
        this.uuid = uuid;
        this.skin = skin;
        this.hologram = hologram;
        this.shown = shown;
        this.equipment = equipment;

        this.gameProfile = new WrappedGameProfile(uuid, "npc_" + id);
        this.gameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", this.skin.getTexture(), this.skin.getSignature()));
    }

    public void delete(boolean despawn) {
        this.hologram.delete();
        if (despawn)
            Bukkit.getScheduler().runTask(RunicNpcs.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::despawnForPlayer));
    }

    public void despawnForPlayer(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists().write(0, new ArrayList<>() {{
            this.add(getEntityId());
        }});
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public Integer getEntityId() {
        return 9000 + this.getId();
    }
    public String getEntityName() { return this.gameProfile.getName();}

    public Hologram getHologram() {
        return this.hologram;
    }

    public int getId() {
        return this.id;
    }

    /**
     * @return the label of the Npc "Merchant," "Quest," etc.
     */
    public NpcTag getLabel() {
        return this.label;
    }

    public void setLabel(NpcTag label) {
        this.label = label;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public HashMap<EnumWrappers.ItemSlot, ItemStack> getEquipment() {
        return equipment;
    }

    public boolean isShown() {
        return this.shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
        RunicNpcs.updateNpcs();
    }

    /*
        Experimental Methods - IN TESTING DO NOT USE
    */
    @Deprecated
    public void moveEntityForPlayer(Player player, double x, double y, double z) {
        this.hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
            new BukkitRunnable() {
                double curX = Npc.this.getLocation().getX();
                double curY = Npc.this.getLocation().getY();
                double curZ = Npc.this.getLocation().getZ();

                @Override
                public void run() {
                    if ((Math.abs(curX - x) < 0.1) && (Math.abs(curY - y) < 0.1) && (Math.abs(curZ - z) < 0.1)) {
                       // Npc.this.hologram.setPosition(new Location(Bukkit.getWorld("Alterra"), x,y + 2.5,z));
                       // Npc.this.hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
                        this.cancel();
                        return;
                    }

                    if (curX < x) curX += 0.1;
                    if (curY < y) curY += 0.1;
                    if (curZ < z) curZ += 0.1;

                    if (curX > x) curX -= 0.1;
                    if (curY > y) curY -= 0.1;
                    if (curZ > z) curZ -= 0.1;

                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                    packet.getIntegers().write(0, Npc.this.getEntityId());
                    packet.getDoubles().write(0, curX);
                    packet.getDoubles().write(1, curY);
                    packet.getDoubles().write(2, curZ);
                    packet.getBytes().write(0, (byte) ((int) Npc.this.getLocation().getYaw() * 256.0F / 360.0F));
                    packet.getBytes().write(1, (byte) ((int) Npc.this.getLocation().getPitch() * 256.0F / 360.0F));
                    packet.getBooleans().write(0, false);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    lookAtForPlayer(player, x, y, z);
                }
            }.runTaskTimer(RunicNpcs.getInstance(), 0L, 1L);

    }

    /*
        Experimental Methods - IN TESTING DO NOT USE
    */
    @Deprecated
    public void lookAtForPlayer(Player player, double x, double y, double z) {
        float[] rotations = getRotation(new Location(this.getLocation().getWorld(), x, y, z));

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, this.getEntityId());
        packet.getBytes().write(0, (byte) (rotations[0] * 256.0F / 360.0F));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    /*
        Experimental Methods - IN TESTING DO NOT USE
    */
    @Deprecated
    private float[] getRotation(Location target) {
        double dx = target.getX() - Npc.this.getLocation().getX();
        double dy = target.getY() - Npc.this.getLocation().getY();
        double dz = target.getZ() - Npc.this.getLocation().getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(dy, distanceXZ));

        return new float[]{(float) yaw, (float) pitch};
    }

    public void rotateHeadForPlayer(Player player, Location location) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, this.getEntityId());
        packet.getBytes().write(0, (byte) ((location.getYaw() * 256.0F) / 360F));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public void spawnForPlayer(Player player) {
        PacketContainer infoPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        infoPacket.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        PlayerInfoData data = new PlayerInfoData(this.uuid, 0, false, EnumWrappers.NativeGameMode.SURVIVAL, this.gameProfile, null);
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

        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, this.getEntityId());
        List<WrappedDataValue> dataValues = new ArrayList<>();
        dataValues.add(new WrappedDataValue(17, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0xFF));
        metadataPacket.getDataValueCollectionModifier().write(0, dataValues);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

        updateEquipment();

        rotateHeadForPlayer(player, this.location);
    }

    public void updateEquipment() {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> npcequipment = getEquipmentPacket();
        if(npcequipment.size() > 0) {
            PacketContainer equipmentContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            equipmentContainer.getIntegers().write(0, getEntityId());
            equipmentContainer.getLists(BukkitConverters.getPairConverter(EnumWrappers.getItemSlotConverter(), BukkitConverters.getItemStackConverter())).write(0, npcequipment);
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(equipmentContainer);
        }
    }

    private List<Pair<EnumWrappers.ItemSlot, ItemStack>> getEquipmentPacket() {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList();
        getEquipment().forEach((slot, item) ->  equipmentList.add(new Pair<>(slot, item)));
        return equipmentList;
    }

}

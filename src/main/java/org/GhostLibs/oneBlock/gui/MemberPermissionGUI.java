package org.GhostLibs.oneBlock.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.model.IslandMember;
import org.GhostLibs.oneBlock.model.OBPermission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemberPermissionGUI implements InventoryProvider {

    private static final int COLUMNS = 9;
    private static final int HEAD_COLUMN = 4;
    private static final int PERMISSIONS_PER_ROW = 7;
    private static final int MAX_INNER_ROWS = 4;

    private final UUID ownerUUID;
    private final IslandMember member;
    private final String memberName;
    private final Map<OBPermission, int[]> positions = new EnumMap<>(OBPermission.class);
    private boolean saving = false;

    private MemberPermissionGUI(UUID ownerUUID, IslandMember member, String memberName) {
        this.ownerUUID = ownerUUID;
        this.member = member;
        this.memberName = memberName;
    }

    public static void open(Player player, UUID ownerUUID, IslandMember member) {
        String name = Bukkit.getOfflinePlayer(member.getUuid()).getName();
        String memberName = name != null ? name : "Unbekannt";

        int permissionCount = OBPermission.values().length;
        int innerRows = Math.min(MAX_INNER_ROWS, Math.max(1, (int) Math.ceil(permissionCount / (double) PERMISSIONS_PER_ROW)));

        SmartInventory.builder()
                .id("oneblock_member_permissions")
                .provider(new MemberPermissionGUI(ownerUUID, member, memberName))
                .manager(OneBlock.getInstance().getInventoryManager())
                .size(innerRows + 2, COLUMNS)
                .title(Sprache.get("UI.memberPermissionsTitle", "%player%", memberName))
                .build()
                .open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(borderItem()));
        contents.set(0, HEAD_COLUMN, ClickableItem.empty(createHeadItem()));

        OBPermission[] permissions = OBPermission.values();
        int limit = Math.min(permissions.length, MAX_INNER_ROWS * PERMISSIONS_PER_ROW);
        for (int i = 0; i < limit; i++) {
            int row = 1 + i / PERMISSIONS_PER_ROW;
            int column = 1 + i % PERMISSIONS_PER_ROW;
            positions.put(permissions[i], new int[]{row, column});
            contents.set(row, column, createButton(player, permissions[i], contents));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void toggle(Player viewer, OBPermission permission, InventoryContents contents) {
        if (saving) return;

        boolean newValue = !member.hasPermission(permission);
        member.setPermission(permission, newValue);
        refreshButton(viewer, permission, contents);

        saving = true;
        OBManager manager = OBManager.getInstance();
        manager.updateMemberPermissions(ownerUUID, member).whenComplete((success, exception) ->
                Bukkit.getScheduler().runTask(OneBlock.getInstance(), () -> {
                    saving = false;
                    if (exception != null || !Boolean.TRUE.equals(success)) {
                        member.setPermission(permission, !newValue);
                        refreshButton(viewer, permission, contents);
                        manager.sendMessageSync(viewer, Sprache.get("COMMANDS.MEMBER_PERMISSION_ERROR"));
                    }
                }));
    }

    private void refreshButton(Player viewer, OBPermission permission, InventoryContents contents) {
        int[] position = positions.get(permission);
        if (position == null) return;
        contents.set(position[0], position[1], createButton(viewer, permission, contents));
    }

    private ClickableItem createButton(Player viewer, OBPermission permission, InventoryContents contents) {
        return ClickableItem.of(createPermissionItem(permission), event -> toggle(viewer, permission, contents));
    }

    private ItemStack createPermissionItem(OBPermission permission) {
        boolean allowed = member.hasPermission(permission);
        ItemStack item = new ItemStack(permission.getIcon());

        item.editMeta(meta -> {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(Sprache.get("PERMISSIONS." + permission.name() + ".DESC"));
            lore.add("");
            lore.add(Sprache.get("UI.permissionState", "%state%",
                    allowed ? Sprache.get("UI.permissionAllowed") : Sprache.get("UI.permissionDenied")));
            lore.add(Sprache.get("UI.permissionToggleHint"));

            meta.setDisplayName(Sprache.get("PERMISSIONS." + permission.name() + ".NAME"));
            meta.setLore(lore);
            meta.setEnchantmentGlintOverride(allowed);
        });

        return item;
    }

    private ItemStack createHeadItem() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());

        head.editMeta(SkullMeta.class, meta -> {
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(Sprache.get("UI.memberHeadTitle", "%player%", memberName));
        });

        return head;
    }

    private ItemStack borderItem() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.setDisplayName(" "));
        return pane;
    }
}

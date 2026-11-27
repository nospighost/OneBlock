package org.GhostLibs.oneBlock.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class IslandListGUI implements InventoryProvider {

    private static final int COLUMNS = 9;
    private static final int ITEMS_PER_ROW = 7;
    private static final int MAX_INNER_ROWS = 4;
    private static final int ITEMS_PER_PAGE = ITEMS_PER_ROW * MAX_INNER_ROWS;

    private final UUID viewerUUID;
    private final PlayerIsland ownIsland;
    private final List<PlayerIsland> memberIslands;

    private IslandListGUI(UUID viewerUUID, PlayerIsland ownIsland, List<PlayerIsland> memberIslands) {
        this.viewerUUID = viewerUUID;
        this.ownIsland = ownIsland;
        this.memberIslands = memberIslands;
    }

    public static void open(Player player) {
        OBManager manager = OBManager.getInstance();
        UUID uuid = player.getUniqueId();

        manager.getPlayerIsland(uuid)
                .thenCombine(manager.getMembersIslands(uuid), (ownIsland, memberIslands) -> new IslandListGUI(uuid, ownIsland, memberIslands))
                .thenAccept(gui -> Bukkit.getScheduler().runTask(OneBlock.getInstance(), () -> {
                    if (!player.isOnline()) return;

                    int entryCount = 1 + gui.memberIslands.size();
                    int innerRows = Math.min(MAX_INNER_ROWS, Math.max(1, (int) Math.ceil(entryCount / (double) ITEMS_PER_ROW)));

                    SmartInventory.builder()
                            .id("oneblock_island_list")
                            .provider(gui)
                            .manager(OneBlock.getInstance().getInventoryManager())
                            .size(innerRows + 2, COLUMNS)
                            .title("§b§lInseln")
                            .build()
                            .open(player);
                }))
                .exceptionally(exception -> {
                    OneBlock.getInstance().getLogger().log(Level.SEVERE, "Opening island list failed.", exception);
                    return null;
                });
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(borderItem()));

        List<ClickableItem> entries = new ArrayList<>();
        entries.add(ownIsland != null
                ? createIslandButton(player, ownIsland)
                : ClickableItem.empty(createNoIslandItem()));
        for (PlayerIsland island : memberIslands) {
            entries.add(createIslandButton(player, island));
        }
        ClickableItem[] items = entries.toArray(new ClickableItem[0]);

        Pagination pagination = contents.pagination();
        pagination.setItems(items);
        pagination.setItemsPerPage(ITEMS_PER_PAGE);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

        if (items.length > ITEMS_PER_PAGE) {
            int lastRow = contents.inventory().getRows() - 1;

            if (!pagination.isFirst()) {
                contents.set(lastRow, 3, ClickableItem.of(namedItem(Material.ARROW, Sprache.get("UI.previousPage")),
                        event -> contents.inventory().open(player, pagination.previous().getPage())));
            }
            if (!pagination.isLast()) {
                contents.set(lastRow, 5, ClickableItem.of(namedItem(Material.ARROW, Sprache.get("UI.nextPage")),
                        event -> contents.inventory().open(player, pagination.next().getPage())));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private ClickableItem createIslandButton(Player player, PlayerIsland island) {
        return ClickableItem.of(createIslandItem(island), event -> {
            OBManager.getInstance().teleportToOneblock(island.getOwnerId(), player);
            player.closeInventory();
        });
    }

    private ItemStack createNoIslandItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        item.editMeta(meta -> {
            meta.setDisplayName(Sprache.get("UI.noIslandTitle"));
            meta.setLore(List.of("", Sprache.get("UI.noIslandHint")));
        });
        return item;
    }

    private ItemStack createIslandItem(PlayerIsland island) {
        boolean islandOwner = viewerUUID.equals(island.getOwnerUUID());
        ItemStack islandItem = new ItemStack(islandOwner ? Material.BEACON : Material.GRASS_BLOCK);

        islandItem.editMeta(meta -> {
            List<String> lore = new ArrayList<>();

            String ownerName = island.getOwnerUUID() != null ? Bukkit.getOfflinePlayer(island.getOwnerUUID()).getName() : null;
            if (ownerName == null) {
                ownerName = "Unbekannt";
            }
            meta.setDisplayName(Sprache.get("UI.islandItemTitle", "%player%", ownerName));

            lore.add("");
            lore.add(Sprache.get("UI.islandName", "%name%", island.getIslandName()));
            lore.add(Sprache.get("UI.islandLocked", "%state%", island.isLocked() ? Sprache.get("UI.islandLockedYes") : Sprache.get("UI.islandLockedNo")));

            meta.setLore(lore);
        });

        return islandItem;
    }

    private ItemStack namedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.setDisplayName(name));
        return item;
    }

    private ItemStack borderItem() {
        return namedItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }
}
package org.zeroBzeroT.antiillegals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.zeroBzeroT.antiillegals.helpers.BookHelper;
import org.zeroBzeroT.antiillegals.helpers.InventoryHolderHelper;
import org.zeroBzeroT.antiillegals.helpers.RevertHelper;
import org.zeroBzeroT.antiillegals.result.ReversionResult;
import org.zeroBzeroT.antiillegals.result.ItemState;

public class Events implements Listener {

    private static void handlePlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame ||
                event.getRightClicked() instanceof GlowItemFrame ||
                event.getRightClicked() instanceof ArmorStand) {

            final Player player = event.getPlayer();
            final PlayerInventory inventory = player.getInventory();
            final Location location = player.getLocation();

            final ItemStack mainHandStack = inventory.getItemInMainHand();
            final ItemStack offhandHandStack = inventory.getItemInOffHand();

            if (RevertHelper.revertAll(location, true, ItemState::isIllegal, mainHandStack, offhandHandStack)) {
                event.setCancelled(true);
                mainHandStack.setAmount(0);
                offhandHandStack.setAmount(0);
                AntiIllegals.log(event.getEventName(), "Removed illegal items from " + player.getName()
                        + " while interacting with " + event.getRightClicked().getType() + ".");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(@NotNull final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        InventoryHolderHelper.getInventory(block)
                .ifPresent(inventory -> RevertHelper.checkInventory(inventory, location, true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractBlock(@NotNull final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHISELED_BOOKSHELF)
            return;

        InventoryHolderHelper.getInventory(block).ifPresent(inventory -> {
            final Location location = block.getLocation();
            final ReversionResult result = RevertHelper.checkInventory(inventory, location, true, false);

            if (!result.wasReverted())
                return;

            block.getState().update(true, false);
            AntiIllegals.log(event.getEventName(), "Removed illegal items from chiseled bookshelf used by "
                    + event.getPlayer().getName() + ".");
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(@NotNull final InventoryOpenEvent event) {
        final Inventory inventory = event.getInventory();

        final HumanEntity player = event.getPlayer();
        final PlayerInventory playerInventory = player.getInventory();
        final Location location = player.getLocation();

        if (inventory.getType() == InventoryType.ENDER_CHEST) {
            BookHelper.checkEnderChest(event, location);
            return;
        }

        RevertHelper.checkInventory(inventory, location, true);
        RevertHelper.checkArmorContents(playerInventory, location, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(@NotNull final VehicleDestroyEvent event) {
        final Vehicle vehicle = event.getVehicle();
        final Location location = vehicle.getLocation();

        InventoryHolderHelper.getInventory(vehicle)
                .ifPresent(inventory -> RevertHelper.checkInventory(inventory, location, true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDrop(@NotNull final PlayerDropItemEvent event) {
        final Item itemDrop = event.getItemDrop();
        final ItemStack itemStack = itemDrop.getItemStack();

        RevertHelper.checkItemStack(itemStack, itemDrop.getLocation(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(@NotNull final EntityDeathEvent event) {
        RevertHelper.revertAll(event.getEntity().getLocation(), false, event.getDrops());
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(@NotNull final HangingBreakEvent event) {
        RevertHelper.revertItemDisplayEntity(event.getEntity(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull final EntityDamageByEntityEvent event) {
        RevertHelper.revertItemDisplayEntity(event.getEntity(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull final PlayerInteractEntityEvent event) {
        handlePlayerInteractEntity(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(@NotNull final PlayerInteractAtEntityEvent event) {
        handlePlayerInteractEntity(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlaceBlock(final BlockPlaceEvent event) {
        final Block placedBlock = event.getBlockPlaced();
        final Material placedBlockType = placedBlock.getType();
        final Location location = placedBlock.getLocation();

        final ItemStack itemStackUsed = event.getItemInHand();

        final Player player = event.getPlayer();
        final String playerName = player.getName();

        if (placedBlockType == Material.END_PORTAL_FRAME && itemStackUsed.getType() == Material.ENDER_EYE) {
            AntiIllegals.log(event.getEventName(), playerName + " put an ender eye on a portal frame.");
            return;
        }

        if (RevertHelper.revertAll(location, true, ItemState::isIllegal, itemStackUsed)) {
            event.setCancelled(true);
            itemStackUsed.setAmount(0);
            AntiIllegals.log(event.getEventName(), "Removed illegal block '" + placedBlockType + "' while being placed by " + playerName + ".");
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityPickupItem(@NotNull final EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();
        final ItemStack itemStack = event.getItem().getItemStack();

        if (RevertHelper.revertAll(entity.getLocation(), true, ItemState::isIllegal, itemStack)) {
            event.setCancelled(true);
            itemStack.setAmount(0);
            String entityName = entity.getName();
            AntiIllegals.log(event.getEventName(), "Removed an illegal item picked up by " + entityName + ".");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwapHandItems(@NotNull final PlayerSwapHandItemsEvent event) {
        final ItemStack mainHandStack = event.getMainHandItem();
        final ItemStack offhandHandStack = event.getOffHandItem();
        final Location location = event.getPlayer().getLocation();
        final Player player = event.getPlayer();

        if (RevertHelper.revertAll(location, true, ItemState::isIllegal, mainHandStack, offhandHandStack)) {
            event.setCancelled(true);
            mainHandStack.setAmount(0);
            offhandHandStack.setAmount(0);
            AntiIllegals.log(event.getEventName(), "Removed illegal items from " + player.getName()
                    + " during a hand swap.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(@NotNull final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        final Location location = player.getLocation();

        final ItemStack newItem = inventory.getItem(event.getNewSlot());
        final ItemStack previousItem = inventory.getItem(event.getPreviousSlot());

        if (RevertHelper.revertAll(location, true, ItemState::isIllegal, newItem, previousItem)) {
            event.setCancelled(true);
            if (newItem != null)
                newItem.setAmount(0);
            if (previousItem != null)
                previousItem.setAmount(0);
            AntiIllegals.log(event.getEventName(), "Removed illegal items from " + player.getName()
                    + ".");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(@NotNull final InventoryMoveItemEvent event) {
        if (RevertHelper.revert(event.getItem(), event.getSource().getLocation(), true, ItemState::isIllegal)) {
            event.setItem(new ItemStack(Material.AIR));
            event.setCancelled(true);
            AntiIllegals.log(event.getEventName(), "Removed an illegal item being transferred between inventories.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerArmorStandManipulate(@NotNull final PlayerArmorStandManipulateEvent event) {
        final ItemStack playerItem = event.getPlayerItem();
        final ItemStack armorStandItem = event.getArmorStandItem();
        final Location location = event.getPlayer().getLocation();

        if (RevertHelper.revertAll(location, true, ItemState::isIllegal, playerItem, armorStandItem)) {
            event.setCancelled(true);
            playerItem.setAmount(0);
            armorStandItem.setAmount(0);
            AntiIllegals.log(event.getEventName(), "Removed illegal items of " + event.getPlayer().getName() + " on armor stand manipulation.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(@NotNull final InventoryClickEvent event) {
        final HumanEntity player = event.getWhoClicked();
        final Location location = player.getLocation();

        final ItemStack clicked = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();

        if (RevertHelper.revertAll(location, true, ItemState::isIllegal, clicked, cursor)) {
            event.setCancelled(true);
            if (clicked != null)
                clicked.setAmount(0);
            cursor.setAmount(0);
            String playerName = player.getName();
            AntiIllegals.log(event.getEventName(), "Removed illegal items from the inventory of " + playerName + ".");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(@NotNull final BlockDispenseEvent event) {
        final ItemStack item = event.getItem();
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        if (RevertHelper.revert(item, location, true, ItemState::isIllegal)) {
            event.setCancelled(true);
            event.setItem(new ItemStack(Material.AIR));
            block.getState().update(true, false);
            AntiIllegals.log(event.getEventName(), "Removed an illegal item while being dispensed.");
        }
    }
}

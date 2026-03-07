package org.zeroBzeroT.antiillegals.result;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.zeroBzeroT.antiillegals.helpers.InventoryHolderHelper;

/**
 * Represents a cached state of an ItemStack, including its original item and
 * its state.
 * Provides methods to build collision-free cache keys based on item identity
 * and NBT data,
 * and to apply a cached state back to an ItemStack.
 */
public record CachedState(@NotNull ItemStack revertedStack, @NotNull ItemState revertedState) {
    /**
     * Builds a collision-free cache key from the item's material, amount, and full
     * NBT
     * string. Two items that are functionally identical will produce the same key;
     * two
     * items that differ in any way will produce different keys, eliminating the
     * false
     * cache-hit problem that arose from using a 32-bit int hash as the key.
     *
     * @param itemStack the itemstack to key
     * @return a unique string key for this item's logical identity
     */
    @NotNull
    public static String itemStackCacheKey(@NotNull final ItemStack itemStack) {
        return itemStack.getType().ordinal() + ":" + itemStack.getAmount() + ":" + nbtString(itemStack);
    }

    /**
     * Returns the raw NBT serialization of the item's meta, or an empty string if
     * the
     * item has no meta. Used as part of the cache key so that every NBT detail is
     * captured without any intermediate hashing.
     *
     * @param itemStack the item stack of which the nbt string will be used
     * @return the NBT string of the item meta
     */
    @NotNull
    private static String nbtString(@NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return "";
        return itemMeta.getAsString();
    }

    /**
     * Applies the cached reverted state to a given ItemStack.
     * If the reverted state is CLEAN, no changes are made.
     * Otherwise, it restores the item meta, inventory contents, and amount.
     *
     * @param cached the ItemStack to which the cached state should be applied
     */
    public void applyRevertedState(@NotNull final ItemStack cached) {
        if (revertedState == ItemState.CLEAN)
            return; // nothing to change

        cached.setItemMeta(revertedStack.getItemMeta());
        InventoryHolderHelper.copyInventoryContents(revertedStack, cached);
        cached.setAmount(revertedStack.getAmount());
    }
}

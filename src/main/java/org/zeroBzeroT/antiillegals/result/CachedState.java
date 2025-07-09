package org.zeroBzeroT.antiillegals.result;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.zeroBzeroT.antiillegals.helpers.InventoryHolderHelper;

import java.util.Objects;

/**
 * Represents a cached state of an ItemStack, including its original item and its state.
 * Provides methods to hash item stacks based on their identity and NBT data,
 * and to apply a cached state back to an ItemStack.
 */
public record CachedState(@NotNull ItemStack revertedStack, @NotNull ItemState revertedState) {
    /**
     * hashes the item identity, not the object reference itself
     *
     * @param itemStack the itemstack to find the hashcode of
     * @return the hashcode
     */
    public static int itemStackHashCode(@NotNull final ItemStack itemStack) {
        return Objects.hash(
                itemStack.getType().ordinal(),
                itemStack.getAmount(),
                nbtHashCode(itemStack)
        );
    }

    /**
     * NBT hashcode implementation.
     *
     * @param itemStack the item stack of which the nbt json will be used
     * @return the hashcode of the nbt
     */
    private static Object nbtHashCode(@NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        return Objects.hash(itemMeta.getAsString());
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

package net.p3pp3rf1y.sophisticatedcore.util;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.fabricators_of_create.porting_lib.transfer.TransferUtil.truncateLong;

public class ItemHelper {
    public static enum ExtractionCountMode {
        EXACTLY, UPTO
    }

    public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, boolean simulate) {
        return extract(inv, test, ExtractionCountMode.UPTO, 64, simulate);
    }

    public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, int exactAmount, boolean simulate) {
        return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
    }

    public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, ExtractionCountMode mode, int amount,
                                    boolean simulate) {
        int extracted = 0;
        ItemVariant extracting = null;
        List<ItemVariant> otherTargets = null;

        if (inv.supportsExtraction()) {
            try (Transaction t = TransferUtil.getTransaction()) {
                for (StorageView<ItemVariant> view : TransferUtil.getNonEmpty(inv)) {
                    ItemVariant contained = view.getResource();
                    int maxStackSize = contained.getItem().getMaxStackSize();
                    // amount stored, amount needed, or max size, whichever is lowest.
                    int amountToExtractFromThisSlot = Math.min(truncateLong(view.getAmount()), Math.min(amount - extracted, maxStackSize));
                    if (!test.test(contained.toStack(amountToExtractFromThisSlot)))
                        continue;
                    if (extracting == null) {
                        extracting = contained; // we found a target
                    }
                    boolean sameType = extracting.equals(contained);
                    if (sameType && maxStackSize == extracted) {
                        // stack is maxed out, skip
                        continue;
                    }
                    if (!sameType) {
                        // multiple types passed the test
                        if (otherTargets == null) {
                            otherTargets = new ArrayList<>();
                        }
                        otherTargets.add(contained);
                        continue;
                    }
                    ItemVariant toExtract = extracting;
                    long actualExtracted = view.extract(toExtract, amountToExtractFromThisSlot, t);
                    if (actualExtracted == 0) continue;
                    extracted += actualExtracted;
                    if (extracted == amount) {
                        if (!simulate)
                            t.commit();
                        return toExtract.toStack(extracted);
                    }
                }

                // if the code reaches this point, we've extracted as much as possible, and it isn't enough.
                if (mode == ExtractionCountMode.UPTO) { // we don't need to get exactly the amount requested
                    if (extracting != null && extracted != 0) {
                        if (!simulate) t.commit();
                        return extracting.toStack(extracted);
                    }
                } else {
                    // let's try a different target
                    if (otherTargets != null) {
                        t.abort();
                        try (Transaction nested = TransferUtil.getTransaction()) {
                            for (ItemVariant target : otherTargets) {
                                // try again, but now only match the existing matches we've found
                                ItemStack successfulExtraction = extract(inv, target::matches, mode, amount, simulate);
                                if (!successfulExtraction.isEmpty()) {
                                    if (!simulate) nested.commit();
                                    return successfulExtraction;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test,
                                    Function<ItemStack, Integer> amountFunction, boolean simulate) {
        ItemStack extracting = ItemStack.EMPTY;
        int maxExtractionCount = 64;

        try (Transaction t = TransferUtil.getTransaction()) {
            for (StorageView<ItemVariant> view : TransferUtil.getNonEmpty(inv)) {
                ItemVariant var = view.getResource();
                ItemStack stackInSlot = var.toStack();
                if (!test.test(stackInSlot))
                    continue;
                if (extracting.isEmpty()) {
                    int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
                    if (maxExtractionCountForItem == 0)
                        continue;
                    maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
                }

                try (Transaction nested = t.openNested()) {
                    long extracted = view.extract(var, maxExtractionCount - extracting.getCount(), nested);
                    ItemStack stack = var.toStack((int) extracted);

                    if (!test.test(stack))
                        continue;
                    if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting))
                        continue;
                    nested.commit();
                    if (extracting.isEmpty())
                        extracting = stack.copy();
                    else
                        extracting.grow(stack.getCount());

                    if (extracting.getCount() >= maxExtractionCount)
                        break;
                }
            }
            if (!simulate) t.commit();
        }

        return extracting;
    }

    public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
        return ItemHandlerHelper.canItemStacksStack(a, b) && a.getCount() + b.getCount() <= a.getMaxStackSize();
    }
}

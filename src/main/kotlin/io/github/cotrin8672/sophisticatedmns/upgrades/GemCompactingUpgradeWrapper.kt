package io.github.cotrin8672.sophisticatedmns.upgrades

import com.robertx22.mine_and_slash.mmorpg.registers.common.items.GemItems
import com.robertx22.mine_and_slash.vanilla_mc.items.gemrunes.GemItem
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.items.IItemHandler
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase
import java.util.function.Consumer

/**
 * MAS の Gem を自動的に 3→1 で上位ランクへ圧縮するアップグレードのラッパー。
 *
 * - フィルタ機能は持たず、常にバックパック内のすべての GemItem を対象とする。
 * - onAfterInsert トリガで圧縮処理を行う。
 */
class GemCompactingUpgradeWrapper(
    storageWrapper: IStorageWrapper,
    upgrade: ItemStack,
    upgradeSaveHandler: Consumer<ItemStack>,
) :
    UpgradeWrapperBase<GemCompactingUpgradeWrapper, GemCompactingUpgradeItem>(
        storageWrapper,
        upgrade,
        upgradeSaveHandler,
    ),
    IInsertResponseUpgrade,
    ISlotChangeResponseUpgrade,
    ITickableUpgrade {

    private val slotsToCompact: MutableSet<Int> = mutableSetOf()

    override fun onBeforeInsert(
        inventoryHandler: IItemHandlerSimpleInserter,
        slot: Int,
        stack: ItemStack,
        simulate: Boolean,
    ): ItemStack {
        // 挿入自体はそのまま通し、挿入後に全体を圧縮する
        return stack
    }

    override fun onAfterInsert(inventoryHandler: IItemHandlerSimpleInserter, slot: Int) {
        compactSlot(inventoryHandler, slot)
    }

    override fun onSlotChange(inventoryHandler: IItemHandler, slot: Int) {
        slotsToCompact.add(slot)
    }

    override fun tick(entity: Entity?, level: Level, pos: BlockPos) {
        if (slotsToCompact.isEmpty()) {
            return
        }

        val handler = storageWrapper.inventoryHandler
        for (slot in slotsToCompact) {
            compactSlot(handler, slot)
        }

        slotsToCompact.clear()
    }

    private fun compactSlot(handler: IItemHandlerSimpleInserter, slot: Int) {
        // Gem の圧縮はスロット単位ではなくインベントリ全体を対象に行う
        compactAllGems(handler)
    }

    private fun compactAllGems(handler: IItemHandlerSimpleInserter) {
        // GemType ごとに、下位ランクから順に 3→1 変換を行い、可能な限り上位まで昇格させる
        for (type in GemItem.GemType.entries) {
            val ranksByTier = GemItem.GemRank.entries.sortedBy { it.tier }
            for (rank in ranksByTier) {
                val lowerItem = GemItems.MAP[type]?.get(rank)?.get() ?: continue
                val higherItem = getHigherGemItem(lowerItem) ?: continue

                compactSingleTier(handler, lowerItem, higherItem)
            }
        }
    }

    private fun getHigherGemItem(lowerItem: GemItem): GemItem? {
        val currentRank = lowerItem.gemRank
        val nextRank =
            GemItem.GemRank.entries.firstOrNull { it.tier == currentRank.tier + 1 }
                ?: return null
        val byType = GemItems.MAP[lowerItem.gemType] ?: return null
        val regObj = byType[nextRank] ?: return null
        val item: Item = regObj.get()
        return item as? GemItem
    }

    private fun compactSingleTier(
        handler: IItemHandlerSimpleInserter,
        lowerItem: GemItem,
        higherItem: GemItem,
    ) {
        // 対象となる下位 Gem の総数とスロット一覧を取得
        var total = 0
        val slots = mutableListOf<Int>()
        val slotCount = handler.slots
        for (slot in 0 until slotCount) {
            val stack = handler.getStackInSlot(slot)
            if (!stack.isEmpty && stack.item === lowerItem) {
                total += stack.count
                slots += slot
            }
        }

        if (total < 3) return

        var remainingLower = total

        while (remainingLower >= 3) {
            // まず 1 個だけ上位 Gem をシミュレーション挿入して、入るかどうか確認
            val simulateStack = ItemStack(higherItem, 1)
            val remaining = handler.insertItem(simulateStack, true)
            if (!remaining.isEmpty) {
                break
            }

            // 下位 Gem を 3 個分削除
            var toRemove = 3
            for (slot in slots) {
                if (toRemove <= 0) break
                val stack = handler.getStackInSlot(slot)
                if (stack.isEmpty || stack.item !== lowerItem) continue
                val removeCount = minOf(stack.count, toRemove)
                if (removeCount <= 0) continue
                handler.extractItem(slot, removeCount, false)
                toRemove -= removeCount
            }

            if (toRemove > 0) {
                // 理論上発生しないはずだが、安全のためこれ以上は進めない
                break
            }

            // 実際に上位 Gem を 1 個挿入
            val toInsert = ItemStack(higherItem, 1)
            val remainingAfterInsert = handler.insertItem(toInsert, false)
            if (!remainingAfterInsert.isEmpty) {
                break
            }

            remainingLower -= 3
            if (remainingLower < 3) {
                break
            }
        }
    }
}

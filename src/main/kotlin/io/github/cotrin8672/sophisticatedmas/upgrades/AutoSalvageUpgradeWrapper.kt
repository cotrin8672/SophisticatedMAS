package io.github.cotrin8672.sophisticatedmas.upgrades

import net.minecraft.world.item.ItemStack
import net.p3pp3rf1y.sophisticatedbackpacks.api.IInventoryWrapperUpgrade
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase
import java.util.function.Consumer

/**
 * 自動サルベージアップグレードのラッパー
 *
 * IInventoryWrapperUpgradeを実装することで、バックパックのインベントリへの
 * アイテム挿入をインターセプトし、MASギアを自動的にサルベージします。
 */
class AutoSalvageUpgradeWrapper(
    storageWrapper: IStorageWrapper,
    upgrade: ItemStack,
    upgradeSaveHandler: Consumer<ItemStack>
) : UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>(
    storageWrapper,
    upgrade,
    upgradeSaveHandler
), IInventoryWrapperUpgrade {

    /**
     * インベントリハンドラーをラップして、アイテム挿入をインターセプトします
     *
     * @param wrappedHandler ラップする元のインベントリハンドラー
     * @return ラップされたインベントリハンドラー
     */
    override fun wrapInventory(inventory: ITrackedContentsItemHandler): ITrackedContentsItemHandler {
        return SalvagingInventoryHandler(inventory, storageWrapper)
    }
}

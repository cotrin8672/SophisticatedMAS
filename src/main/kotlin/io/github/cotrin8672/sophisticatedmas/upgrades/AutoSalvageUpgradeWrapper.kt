package io.github.cotrin8672.sophisticatedmas.upgrades

import com.robertx22.mine_and_slash.itemstack.ExileStack
import com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ICommonDataItem
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
    upgradeSaveHandler: Consumer<ItemStack>,
) : UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>(
    storageWrapper,
    upgrade,
    upgradeSaveHandler
), IInventoryWrapperUpgrade {

    private var installingListener = false

    init {
        // 二重登録防止ガード
        if (!installingListener) {
            installingListener = true
            storageWrapper.inventoryHandler.addListener { slot: Int ->
                // ここは「どの経路でも」中身が変わるたびに呼ばれる
                handleSlotChanged(slot)
            }
            installingListener = false
        }
    }

    private fun handleSlotChanged(slot: Int) {
        val inv = storageWrapper.inventoryHandler
        val stack = inv.getStackInSlot(slot)
        if (stack.isEmpty) return

        val data = ICommonDataItem.load(stack) ?: return
        val exile = ExileStack.of(stack)
        if (!data.isSalvagable(exile)) return

        // ★サルベージ実行 → 結果の素材を inv に再配置
        //   - この中で inv.setStackInSlot(slot, …) 等で置き換え
        //   - 再入れ替えで再帰的に呼ばれうるので、必要なら reentrancy ガードを入れる
    }

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

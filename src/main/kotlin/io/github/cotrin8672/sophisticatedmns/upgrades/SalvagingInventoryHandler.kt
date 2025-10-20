package io.github.cotrin8672.sophisticatedmns.upgrades

import com.robertx22.mine_and_slash.itemstack.ExileStack
import com.robertx22.mine_and_slash.saveclasses.gearitem.gear_bases.Rarity
import com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ICommonDataItem
import net.minecraft.world.item.ItemStack
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler

/**
 * サルベージ機能を持つインベントリハンドラー
 *
 * バックパックへのアイテム挿入をインターセプトし、MASギアを
 * 自動的にサルベージします。
 */
class SalvagingInventoryHandler(
    private val wrappedHandler: ITrackedContentsItemHandler,
    private val storageWrapper: IStorageWrapper,
) : ITrackedContentsItemHandler by wrappedHandler {

    /**
     * アイテム挿入時に呼び出されます
     * MASギアの場合、サルベージ処理を実行します。
     *
     * @param slot 挿入先のスロット番号
     * @param stack 挿入するアイテムスタック
     * @param simulate trueの場合、実際には挿入せずシミュレーションのみ
     * @return 挿入できなかった残りのアイテムスタック
     */
    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        // シミュレーション時は一切副作用を出さない
        if (simulate || stack.isEmpty) {
            return wrappedHandler.insertItem(slot, stack, true)
        }

        val data: ICommonDataItem<Rarity>? = ICommonDataItem.load(stack)
        if (data == null) {
            return wrappedHandler.insertItem(slot, stack, false)
        }

        val ex = ExileStack.of(stack)
        if (!data.isSalvagable(ex)) {
            return wrappedHandler.insertItem(slot, stack, false)
        }

        // Minecraft.getInstance().player?.sendSystemMessage(Component.literal("gear item"))

        // TODO: MASギアかどうかをチェック
        // TODO: サルベージ可能かどうかをチェック
        // TODO: プレイヤーの自動サルベージ設定をチェック
        // TODO: サルベージを実行
        // TODO: サルベージ結果の素材をバックパックに挿入


        // 現時点では、元のハンドラーにそのまま委譲
        return wrappedHandler.insertItem(slot, stack, simulate)
    }
}

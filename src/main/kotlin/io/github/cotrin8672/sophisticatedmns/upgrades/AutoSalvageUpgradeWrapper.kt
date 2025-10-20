package io.github.cotrin8672.sophisticatedmns.upgrades

import com.robertx22.library_of_exile.utils.SoundUtils
import com.robertx22.mine_and_slash.database.registry.ExileDB
import com.robertx22.mine_and_slash.itemstack.ExileStack
import com.robertx22.mine_and_slash.saveclasses.gearitem.gear_bases.Rarity
import com.robertx22.mine_and_slash.uncommon.datasaving.Load
import com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ICommonDataItem
import com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ISalvagable
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.p3pp3rf1y.sophisticatedbackpacks.api.IInventoryWrapperUpgrade
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase
import java.lang.ref.WeakReference
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
), IInventoryWrapperUpgrade, IInsertResponseUpgrade, ITickableUpgrade {
    private var playerRef: WeakReference<Player?> = WeakReference(null)

    override fun wrapInventory(inventory: ITrackedContentsItemHandler): ITrackedContentsItemHandler {
        return SalvagingInventoryHandler(inventory, storageWrapper)
    }

    override fun onBeforeInsert(
        inserter: IItemHandlerSimpleInserter,
        slot: Int,
        stack: ItemStack,
        simulate: Boolean,
    ): ItemStack {
        if (simulate) return stack

        val playerInstance = playerRef.get() ?: return stack
        val result = autoSalvageIfAllowed(playerInstance, stack)

        if (result.isEmpty()) {
            return stack
        } else {
            result.forEach {
                inserter.insertItem(it, false)
            }
            return ItemStack.EMPTY
        }
    }

    override fun onAfterInsert(inserter: IItemHandlerSimpleInserter, slot: Int) {

    }

    override fun tick(entity: Entity?, level: Level, pos: BlockPos) {
        if (entity is Player?) {
            playerRef = WeakReference(entity)
        }
    }

    private fun autoSalvageIfAllowed(player: Player, stack: ItemStack): List<ItemStack> {
        // 既にエンチャント済みの装備は自動サルベ対象から除外
        if (stack.isEnchanted) return emptyList()

        val ex = ExileStack.of(stack)
        val data: ICommonDataItem<Rarity>? = ICommonDataItem.load(stack)
        val sal: ISalvagable? = ISalvagable.load(stack)

        // サルベージ可能判定
        if (data == null || sal == null || !data.isSalvagable(ex)) {
            return emptyList()
        }

        // プレイヤー設定の取得：個別ID優先、なければレアリティ設定
        val salvageType = data.salvageType
        val configId = data.salvageConfigurationId

        val cfg = Load.player(player).config.salvage
        val typeOverride = cfg.checkTypeSalvageConfig(salvageType, configId) // Optional<Boolean>

        val doSalvage = if (typeOverride.isPresent) {
            typeOverride.get()
        } else {
            cfg.checkRaritySalvageConfig(salvageType, data.rarityId)
        }
        // ↑ 個別設定があればそれを優先、無ければレアリティ設定を参照

        if (!doSalvage) return emptyList()

        SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75f, 1.25f)

        // Salvaging職のEXPを付与（restedボーナスなし）
        ExileDB.Professions().get("salvaging")?.let { salvaging ->
            // data.getAutoSalvageExpReward() に相当するKotlin呼び出し名は環境に合わせて
            val exp = data.autoSalvageExpReward
            Load.player(player).professions.addExp(player, salvaging.GUID(), exp, false)
        }

        // 実際のサルベージ結果を生成（元実装に倣い ISalvagable の結果を使用）
        val results = sal.getSalvageResult(ex).toMutableList()

        // 元スタックを消費（自動サルベージでは拾得直後に消す挙動）
        if (!stack.isEmpty) {
            stack.shrink(stack.count)
        }

        return results
    }
}

package io.github.cotrin8672.sophisticatedmns.upgrades

import net.p3pp3rf1y.sophisticatedbackpacks.Config
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem.UpgradeConflictDefinition
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType

/**
 * 自動サルベージアップグレードアイテム
 *
 * バックパックに装着することで、Mine and Slashのギアアイテムを
 * 自動的にサルベージ（分解）するアップグレード。
 */
class AutoSalvageUpgradeItem : UpgradeItemBase<AutoSalvageUpgradeWrapper>(Config.SERVER.maxUpgradesPerStorage) {
    companion object {
        /**
         * アップグレードタイプ
         * Sophisticated Backpacksのアップグレードシステムに登録されます。
         */
        val TYPE: UpgradeType<AutoSalvageUpgradeWrapper> =
            UpgradeType(::AutoSalvageUpgradeWrapper)
    }

    /**
     * このアップグレードのタイプを返します
     */
    override fun getType(): UpgradeType<AutoSalvageUpgradeWrapper> = TYPE

    /**
     * アップグレードの競合定義を返します
     * 現時点では競合なし
     */
    override fun getUpgradeConflicts(): MutableList<UpgradeConflictDefinition> = mutableListOf()
}

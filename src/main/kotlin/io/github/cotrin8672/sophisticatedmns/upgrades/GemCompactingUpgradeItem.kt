package io.github.cotrin8672.sophisticatedmns.upgrades

import net.p3pp3rf1y.sophisticatedbackpacks.Config
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem.UpgradeConflictDefinition
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType

/** MASのGemを自動圧縮（3つで上位Gemに変換）するバックパックアップグレードアイテム。 */
class GemCompactingUpgradeItem :
        UpgradeItemBase<GemCompactingUpgradeWrapper>(Config.SERVER.maxUpgradesPerStorage) {
    companion object {
        /** Sophisticated Backpacks のアップグレードシステムに登録されるタイプ。 */
        val TYPE: UpgradeType<GemCompactingUpgradeWrapper> =
                UpgradeType(::GemCompactingUpgradeWrapper)
    }

    override fun getType(): UpgradeType<GemCompactingUpgradeWrapper> = TYPE

    override fun getUpgradeConflicts(): MutableList<UpgradeConflictDefinition> = mutableListOf()
}

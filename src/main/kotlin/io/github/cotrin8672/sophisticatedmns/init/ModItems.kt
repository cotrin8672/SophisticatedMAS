package io.github.cotrin8672.sophisticatedmns.init

import com.tterrag.registrate.util.entry.ItemEntry
import io.github.cotrin8672.sophisticatedmns.SophisticatedMnS
import io.github.cotrin8672.sophisticatedmns.upgrades.AutoSalvageUpgradeItem
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK_UPGRADE_TAG
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CREATIVE_TAB


/**
 * MODアイテムの登録を管理するオブジェクト
 */
object ModItems {
    /**
     * 自動サルベージアップグレードアイテム
     */
    val AUTO_SALVAGE_UPGRADE: ItemEntry<AutoSalvageUpgradeItem> = SophisticatedMnS.Registrate
        .item<AutoSalvageUpgradeItem>("auto_salvage_upgrade") { AutoSalvageUpgradeItem() }
        .tab(CREATIVE_TAB.key!!)
        .properties { p -> p.stacksTo(1) }
        .tag(BACKPACK_UPGRADE_TAG)
        .defaultModel()                                                 // デフォルトの生成モデル
        .defaultLang()
        .register()

    /**
     * すべてのアイテムを登録する
     * MODの初期化時に呼び出される必要があります。
     */
    fun register() {

    }
}

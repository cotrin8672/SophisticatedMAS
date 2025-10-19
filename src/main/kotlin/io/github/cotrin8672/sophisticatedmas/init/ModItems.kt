package io.github.cotrin8672.sophisticatedmas.init

import com.tterrag.registrate.util.entry.ItemEntry
import io.github.cotrin8672.sophisticatedmas.SophisticatedMASMod
import io.github.cotrin8672.sophisticatedmas.upgrades.AutoSalvageUpgradeItem
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK_UPGRADE_TAG

/**
 * MODアイテムの登録を管理するオブジェクト
 */
object ModItems {
    /**
     * 自動サルベージアップグレードアイテム
     */
    val AUTO_SALVAGE_UPGRADE: ItemEntry<AutoSalvageUpgradeItem> = SophisticatedMASMod.Registrate
        .item("auto_salvage_upgrade") { AutoSalvageUpgradeItem() }
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
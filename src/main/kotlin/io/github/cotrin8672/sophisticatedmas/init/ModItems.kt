package io.github.cotrin8672.sophisticatedmas.init

import com.tterrag.registrate.util.entry.ItemEntry
import io.github.cotrin8672.sophisticatedmas.SophisticatedMASMod
import io.github.cotrin8672.sophisticatedmas.upgrades.AutoSalvageUpgradeItem
import net.minecraft.world.item.CreativeModeTabs

/**
 * MODアイテムの登録を管理するオブジェクト
 */
object ModItems {
    /**
     * 自動サルベージアップグレードアイテム
     */
    val AUTO_SALVAGE_UPGRADE: ItemEntry<AutoSalvageUpgradeItem> = SophisticatedMASMod.REGISTRATE
        .item("auto_salvage_upgrade") { AutoSalvageUpgradeItem() }
        .properties { p -> p.stacksTo(1) }
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .lang("Auto Salvage Upgrade")
        .register()

    /**
     * すべてのアイテムを登録する
     * MODの初期化時に呼び出される必要があります。
     */
    fun register() {
        SophisticatedMASMod.LOGGER.info("Registering MOD items...")
    }
}

package io.github.cotrin8672.sophisticatedmas

import com.tterrag.registrate.Registrate
import io.github.cotrin8672.sophisticatedmas.init.ModIdentity
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Sophisticated MAS MODのエントリーポイント
 *
 * Mine and SlashとSophisticated Backpacksを統合するアドオンMOD。
 * バックパック内のMASギアの自動サルベージと、
 * 作業台への直接アクセス機能を提供します。
 */
@Mod(ModIdentity.MOD_ID)
object SophisticatedMASMod {
    /**
     * MODロガー
     */
    val LOGGER: Logger = LogManager.getLogger(ModIdentity.LOGGER_NAME)

    /**
     * Registrateインスタンス
     * アイテム、ブロック、その他のレジストリオブジェクトの登録を管理します。
     */
    val REGISTRATE: Registrate = Registrate.create(ModIdentity.MOD_ID)

    init {
        LOGGER.info("${ModIdentity.MOD_NAME} is initializing...")
        LOGGER.info("Registrate instance created for MOD ID: ${ModIdentity.MOD_ID}")

        // アイテム登録
        io.github.cotrin8672.sophisticatedmas.init.ModItems.register()
    }
}

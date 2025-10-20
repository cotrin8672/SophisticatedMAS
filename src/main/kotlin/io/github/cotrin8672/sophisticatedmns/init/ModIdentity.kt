package io.github.cotrin8672.sophisticatedmas.init

/**
 * MOD識別子の単一情報源
 *
 * このオブジェクトは、MODの識別子、表示名、ロガー名を一元管理します。
 * gradle.propertiesの値と整合している必要があります。
 */
object ModIdentity {
    /**
     * MOD ID - Forgeの命名規則（小文字英数字とアンダースコア）に準拠
     * gradle.propertiesのmodIdと同一である必要があります
     */
    const val MOD_ID: String = "sophisticatedmns"

    /**
     * MOD表示名 - ユーザーに表示される名前
     * gradle.propertiesのmodNameと同一である必要があります
     */
    const val MOD_NAME: String = "Sophisticated MnS"

    /**
     * ロガー名 - ログ出力の識別に使用
     */
    const val LOGGER_NAME: String = "SophisticatedMnS"
}

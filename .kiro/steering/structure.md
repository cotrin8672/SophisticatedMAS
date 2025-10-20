# Project Structure

## Organization Philosophy

**機能別モジュール構成** + **アップグレードパターン分離**

Sophisticated Backpacksのアップグレードシステムに準拠し、各機能（自動サルベージ、作業台アクセス）を独立したモジュールとして実装。アップグレードごとに以下3コンポーネントに分離：

- **Item** (アイテム定義)
- **Wrapper** (ロジック・状態管理)
- **Container/Tab** (GUI、設定がある場合のみ)

## Directory Patterns

### メインMODパッケージ

**Location**: `src/main/kotlin/io/github/cotrin8672/sophisticatedmas/`
**Purpose**: MODエントリポイント、共通登録処理、ユーティリティ
**Example**: `SophisticatedMAS.kt` - `@Mod` アノテーション付きシングルトン

### アップグレードモジュール

**Location**: `src/main/kotlin/io/github/cotrin8672/sophisticatedmas/upgrades/{feature}/`
**Purpose**: 各機能のアップグレード実装（Item/Wrapper/GUI）
**Example**:

```
upgrades/
  autosalvage/
    AutoSalvageUpgradeItem.kt
    AutoSalvageUpgradeWrapper.kt
    AutoSalvageUpgradeContainer.kt (optional)
  craftingstation/
    CraftingStationUpgradeItem.kt
    CraftingStationUpgradeWrapper.kt
```

### 統合レイヤー

**Location**: `src/main/kotlin/io/github/cotrin8672/sophisticatedmas/integration/`
**Purpose**: 外部MOD API との連携コード
**Example**: `MASIntegration.kt` (サルベージAPI呼び出し), `VirtualProfessionBlockEntity.kt`

### 登録処理

**Location**: `src/main/kotlin/io/github/cotrin8672/sophisticatedmas/init/`
**Purpose**: Forge DeferredRegister による登録
**Example**: `ModItems.kt`, `ModMenus.kt`, `ModUpgrades.kt`

### リソースファイル

**Location**: `src/main/resources/assets/sophisticatedmas/`
**Purpose**: テクスチャ、言語ファイル、モデル
**Structure**:

```
assets/sophisticatedmas/
  lang/
    en_us.json  # 英語ローカライゼーション
    ja_jp.json  # 日本語ローカライゼーション
  textures/item/
    auto_salvage_upgrade.png
    crafting_station_upgrade.png
```

### データジェネレーター

**Location**: `src/main/kotlin/io/github/cotrin8672/sophisticatedmas/datagen/`
**Purpose**: レシピ、タグ、言語ファイルの自動生成
**Example**: `ModRecipeProvider.kt`, `ModLanguageProvider.kt`

## Naming Conventions

- **Files**: PascalCase (`AutoSalvageUpgradeItem.kt`)
- **Classes**: PascalCase、役割を接尾辞で明示 (`*Item`, `*Wrapper`, `*Container`)
- **Objects**: PascalCase (Kotlinシングルトン用、例: `SophisticatedMAS`)
- **Constants**: UPPER_SNAKE_CASE (`MOD_ID`, `UPGRADE_TYPE`)
- **Functions**: camelCase (`checkSalvageSettings`, `openStationGui`)

### パッケージ命名

- トップレベル: `io.github.cotrin8672.sophisticatedmns`
- 機能単位: `upgrades.autosalvage`, `upgrades.craftingstation`
- 共通機能: `integration`, `init`, `util`

## Import Organization

```kotlin
// 1. Java標準ライブラリ
import java.util.function.Consumer

// 2. Minecraft/Forge
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fml.common.Mod

// 3. 依存MOD (MAS)
import com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ISalvagable
import com.robertx22.mine_and_slash.capability.player.data.PlayerConfigData

// 4. 依存MOD (Sophisticated)
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade

// 5. 内部パッケージ（相対インポート）
import io.github.cotrin8672.sophisticatedmns.init.ModItems
```

## Code Organization Principles

### アップグレードパターン順守

Sophisticated Backpacksの既存パターンに従い、`UpgradeType<W>` を静的フィールドとして定義：

```kotlin
class AutoSalvageUpgradeItem : UpgradeItemBase<AutoSalvageUpgradeWrapper>() {
    companion object {
        val TYPE = UpgradeType(::AutoSalvageUpgradeWrapper)
    }
    override fun getType() = TYPE
}
```

### 依存性注入パターン

Wrapperはコンストラクタで `IStorageWrapper` を受け取り、バックパックインベントリにアクセス：

```kotlin
class AutoSalvageUpgradeWrapper(
    storageWrapper: IStorageWrapper,
    upgrade: ItemStack,
    upgradeSaveHandler: Consumer<ItemStack>
) : UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>(
    storageWrapper, upgrade, upgradeSaveHandler
)
```

### レイヤー分離

- **Item層**: アイテムプロパティ、登録、コンフリクト定義のみ
- **Wrapper層**: ビジネスロジック（サルベージ判定、GUI起動）
- **Container層**: GUI状態、スロット管理（必要な場合のみ）

---
_created_at: 2025-01-13_

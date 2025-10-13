# SophisticatedMAS

## プロジェクト概要

このプロジェクトは、**Mine and Slash (MAS)** と **Sophisticated Backpacks** の両方のMODに対応したアドオンMODです。

Mine and SlashとSophisticated Backpacksの連携を強化し、プレイヤーの利便性を向上させることを目的としています。特に、インベントリ管理とアイテム処理の効率化に焦点を当てています。

---

## 主な機能

### 1. 自動サルベージアップグレード（Auto Salvage Upgrade）
バックパックに装着することで、Mine and Slashのアイテムを自動的にサルベージ（分解）してくれるアップグレード機能。

**機能詳細:**
- バックパックへのアイテム格納時に、MASギアを自動的にサルベージ
- プレイヤーの自動サルベージ設定（`PlayerConfigData.AutoSalvage`）との連携
- サルベージ結果（素材）を自動的にバックパックまたはプレイヤーインベントリに格納

### 2. MAS作業台GUIアクセスアップグレード（Crafting Station Access Upgrade）
Sophisticated Backpacksのインターフェースから、Mine and Slashの各種作業台（Crafting Station）のGUIに直接アクセスできるようにするアップグレード機能。

**対応作業台:**
- Salvaging Station（サルベージステーション）
- Gear Crafting Station（ギアクラフティングステーション）
- Cooking Station（料理ステーション）
- その他のMAS Profession作業台

---

## 技術スタック

- **Minecraft Version**: 1.20.1
- **Forge Version**: 47.1.3
- **開発言語**: Kotlin + Java
- **開発ブランチ**: mc1.20.1/dev
- **ビルドシステム**: Gradle (Kotlin DSL)
- **依存MOD**:
  - Mine and Slash (Mine-And-Slash-Rework)
  - Sophisticated Backpacks
  - Kotlin for Forge 4.11.0

### プロジェクト構成

**プロジェクトパス:**
- **このプロジェクト**: `C:\Users\gummy\IdeaProjects\SophisticatedMAS`
- **Mine and Slash**: `C:\Users\gummy\IdeaProjects\Mine-And-Slash-Rework`
- **Sophisticated Backpacks**: `C:\Users\gummy\IdeaProjects\SophisticatedBackpacks`

**Code Context MCPインデックス情報:**
- **Mine and Slash**: ✅ インデックス済み (1403 files, 9500 chunks)
  - 最終更新: 2025/10/11 10:02:55
- **Sophisticated Backpacks**: ✅ インデックス済み (203 files, 1816 chunks)
  - 最終更新: 2025/10/13 0:19:04

**インデックス確認コマンド:**
```bash
# Mine and Slashのインデックス状態確認
mcp__code-context__get_indexing_status(path="C:\Users\gummy\IdeaProjects\Mine-And-Slash-Rework")

# Sophisticated Backpacksのインデックス状態確認
mcp__code-context__get_indexing_status(path="C:\Users\gummy\IdeaProjects\SophisticatedBackpacks")
```

**コード検索例:**
```bash
# Mine and Slashでサルベージ関連コードを検索
mcp__code-context__search_code(
    path="C:\Users\gummy\IdeaProjects\Mine-And-Slash-Rework",
    query="salvage gear item dismantle",
    limit=15
)

# Sophisticated Backpacksでアップグレードシステムを検索
mcp__code-context__search_code(
    path="C:\Users\gummy\IdeaProjects\SophisticatedBackpacks",
    query="upgrade system base wrapper",
    limit=15
)
```

---

## アーキテクチャと設計

### アップグレードシステムの設計パターン

Sophisticated Backpacksのアップグレードシステムは、以下の3つの主要コンポーネントで構成されています：

#### 1. **UpgradeItemBase<W>** - アップグレードアイテム
アップグレードアイテム自体を表すクラス。

```java
public class AutoSalvageUpgradeItem extends UpgradeItemBase<AutoSalvageUpgradeWrapper> {
    private static final UpgradeType<AutoSalvageUpgradeWrapper> TYPE =
        new UpgradeType<>(AutoSalvageUpgradeWrapper::new);

    public AutoSalvageUpgradeItem() {
        super(Config.SERVER.maxUpgradesPerStorage);
    }

    @Override
    public UpgradeType<AutoSalvageUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
```

#### 2. **UpgradeWrapperBase<W, I>** - アップグレードラッパー
アップグレードのロジックと状態を管理するクラス。

```java
public class AutoSalvageUpgradeWrapper extends UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>
    implements ITickableUpgrade, IItemHandlerInteractionUpgrade {

    public AutoSalvageUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(Entity entity, Level level, BlockPos pos) {
        // 定期実行ロジック
    }

    @Override
    public void onHandlerInteract(IItemHandler handler, Player player) {
        // アイテムハンドラーとのインタラクションロジック
    }
}
```

#### 3. **UpgradeContainer & UpgradeTab** - GUI/コンテナ
アップグレードの設定UIを提供するクラス（必要に応じて）。

```java
public class AutoSalvageUpgradeContainer extends UpgradeContainerBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeContainer> {
    // コンテナロジック
}

public class AutoSalvageUpgradeTab extends UpgradeTabBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeContainer> {
    // GUIロジック
}
```

---

## 実装詳細

### 機能1: 自動サルベージアップグレード

#### MAS側のAPI使用

**ISalvagableインターフェース**
```java
// Location: Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/uncommon/interfaces/data_items/ISalvagable.java

public interface ISalvagable {
    List<ItemStack> getSalvageResult(ExileStack stack);
    ToggleAutoSalvageRarity.SalvageType getSalvageType();

    default boolean isSalvagable(ExileStack stack) {
        return !stack.get(StackKeys.CUSTOM).getOrCreate().data.get(CustomItemData.KEYS.SALVAGING_DISABLED);
    }

    static ISalvagable load(ItemStack stack) {
        for (ItemstackDataSaver<? extends ISalvagable> saver : AllItemStackSavers.getAllOfClass(ISalvagable.class)) {
            ISalvagable data = saver.loadFrom(stack);
            if (data != null) {
                return data;
            }
        }
        return null;
    }
}
```

**PlayerConfigData.AutoSalvageの連携**
```java
// Location: Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/PlayerConfigData.java

public boolean trySalvageOnPickup(Player player, ItemStack stack) {
    ExileStack ex = ExileStack.of(stack);

    if (stack.isEnchanted()) {
        return false; // エンチャント済みアイテムは自動サルベージしない
    }

    ICommonDataItem<GearRarity> data = ICommonDataItem.load(stack);
    boolean doSalvage = false;

    if (data != null) {
        if (data.isSalvagable(ex)) {
            Optional<Boolean> typeSalvageEnabled = checkTypeSalvageConfig(data.getSalvageType(), data.getSalvageConfigurationId());

            if (typeSalvageEnabled.isEmpty()) {
                if (checkRaritySalvageConfig(data.getSalvageType(), data.getRarityId())) {
                    doSalvage = true;
                }
            } else {
                doSalvage = typeSalvageEnabled.get();
            }
        }

        if (doSalvage) {
            SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 1.25F);

            // サルベージ職業の経験値を付与
            Profession salvagingProfession = ExileDB.Professions().get("salvaging");
            if (salvagingProfession != null) {
                Load.player(player).professions.addExp(player, salvagingProfession.GUID(), data.getAutoSalvageExpReward(), false);
            }

            stack.shrink(stack.getCount() + 100);
            data.getSalvageResult(ex).forEach(e -> {
                Backpacks backpacks = Load.backpacks(player).getBackpacks();
                if (!backpacks.tryAutoPickup(player, e, false)) PlayerUtils.giveItem(e, player);
            });
            return true;
        }
    }

    return false;
}
```

#### 実装戦略

1. **ITickableUpgradeの実装**: 定期的にバックパックのインベントリをチェック
2. **自動サルベージロジック**:
   - バックパックに追加されたアイテムを検出
   - `ISalvagable.load(stack)`でMASアイテムかチェック
   - `isSalvagable()`でサルベージ可能かチェック
   - プレイヤーの設定（`PlayerConfigData.AutoSalvage`）を考慮
   - `getSalvageResult()`でサルベージ結果を取得
   - 結果アイテムをバックパックに格納

**実装例:**
```java
public class AutoSalvageUpgradeWrapper extends UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>
    implements ITickableUpgrade {

    private int tickCounter = 0;

    @Override
    public void tick(Entity entity, Level level, BlockPos pos) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        // 20tickごとに実行（1秒に1回）
        if (++tickCounter % 20 != 0) return;

        ITrackedContentsItemHandler inventory = storageWrapper.getInventoryHandler();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // MASのISalvagableをチェック
            ISalvagable salvagable = ISalvagable.load(stack);
            if (salvagable == null) continue;

            ExileStack exileStack = ExileStack.of(stack);
            if (!salvagable.isSalvagable(exileStack)) continue;

            // プレイヤーの自動サルベージ設定をチェック
            PlayerConfigData config = Load.player(player).config;
            ICommonDataItem<?> data = ICommonDataItem.load(stack);

            if (data != null) {
                // 設定に基づいてサルベージ実行
                boolean shouldSalvage = checkSalvageSettings(config, data);

                if (shouldSalvage) {
                    List<ItemStack> results = salvagable.getSalvageResult(exileStack);

                    // サルベージ実行
                    stack.shrink(1);

                    // 結果アイテムをバックパックに格納
                    for (ItemStack result : results) {
                        addItemToInventory(inventory, result);
                    }

                    // サウンド再生
                    SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 1.25F);
                }
            }
        }
    }

    private boolean checkSalvageSettings(PlayerConfigData config, ICommonDataItem<?> data) {
        // MASの自動サルベージ設定ロジックを参考に実装
        Optional<Boolean> typeSalvage = config.salvage.checkTypeSalvageConfig(
            data.getSalvageType(),
            data.getSalvageConfigurationId()
        );

        if (typeSalvage.isEmpty()) {
            return config.salvage.checkRaritySalvageConfig(
                data.getSalvageType(),
                data.getRarityId()
            );
        } else {
            return typeSalvage.get();
        }
    }
}
```

---

### 機能2: MAS作業台GUIアクセスアップグレード

#### MAS側のAPI使用

**CraftingStationMenu/Screen**
```java
// Location: Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/database/data/profession/screen/

// Menu (Container)
public class CraftingStationMenu extends AbstractContainerMenu {
    public List<Slot> matslots = new ArrayList<>();
    public List<Slot> invslots = new ArrayList<>();
    public ProfessionBlockEntity be;

    public CraftingStationMenu(String prof, int pContainerId, Container pContainer, ProfessionBlockEntity be) {
        super(SlashContainers.STATIONS.get(prof).get(), pContainerId);
        this.be = be;
        // スロット初期化
    }
}

// Screen (GUI)
public abstract class CraftingStationScreen extends AbstractContainerScreen<CraftingStationMenu> {
    protected ResourceLocation BACKGROUND_LOCATION;
    protected Profession prof;

    public CraftingStationScreen(String prof, CraftingStationMenu pMenu, Inventory pPlayerInventory, Component txt) {
        super(pMenu, pPlayerInventory, ExileDB.Professions().get(prof).locName());
        this.prof = ExileDB.Professions().get(prof);
    }
}

// 各職業の作業台
public class SalvagingScreen extends CraftingStationScreen {
    public SalvagingScreen(CraftingStationMenu pMenu, Inventory pPlayerInventory, Component txt) {
        super(Professions.SALVAGING, pMenu, pPlayerInventory, txt);
        BACKGROUND_LOCATION = new ResourceLocation(SlashRef.MODID, "textures/gui/salvage_station.png");
    }
}

public class GearCraftingScreen extends CraftingStationScreen {
    public GearCraftingScreen(CraftingStationMenu pMenu, Inventory pPlayerInventory, Component txt) {
        super(Professions.GEAR_CRAFTING, pMenu, pPlayerInventory, txt);
    }
}
```

**ProfessionBlockEntity**
```java
// Location: Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/database/data/profession/ProfessionBlockEntity.java

public class ProfessionBlockEntity extends BlockEntity implements MenuProvider {
    public Container inventory = new SimpleContainer(18);
    public Container show = new SimpleContainer(1);

    public Profession getProfession() {
        return ExileDB.Professions().get(professionId);
    }

    public ExplainedResult trySalvage(Player p) {
        // サルベージロジック
    }

    public ExplainedResult tryCraft(Player player) {
        // クラフティングロジック
    }
}
```

#### 実装戦略（2つのアプローチ）

##### アプローチ1: 仮想BlockEntity方式（推奨）

バックパック内に仮想的なProfessionBlockEntityを作成し、既存のMAS GUIシステムを再利用する。

**利点:**
- MASの既存UIを完全に再利用可能
- レシピシステム、クラフティングロジックがそのまま使える
- 保守が容易

**実装例:**
```java
public class CraftingStationUpgradeWrapper extends UpgradeWrapperBase<CraftingStationUpgradeWrapper, CraftingStationUpgradeItem> {
    private final String professionId;

    public CraftingStationUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler, String professionId) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.professionId = professionId;
    }

    public void openStationGui(Player player) {
        if (player.level().isClientSide) return;

        // 仮想BlockEntityを作成
        VirtualProfessionBlockEntity virtualBE = new VirtualProfessionBlockEntity(
            professionId,
            storageWrapper.getInventoryHandler()
        );

        // MASのメニューを開く
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return ExileDB.Professions().get(professionId).locName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new CraftingStationMenu(professionId, containerId, playerInventory, virtualBE);
            }
        });
    }
}

// 仮想BlockEntityの実装
public class VirtualProfessionBlockEntity extends ProfessionBlockEntity {
    private final ITrackedContentsItemHandler backpackInventory;

    public VirtualProfessionBlockEntity(String professionId, ITrackedContentsItemHandler backpackInventory) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.professionId = professionId;
        this.backpackInventory = backpackInventory;
        // バックパックのインベントリをラップ
    }

    @Override
    public Container getInventory() {
        // バックパックのインベントリを返す
        return new BackpackInventoryWrapper(backpackInventory);
    }
}
```

##### アプローチ2: カスタムGUI方式

完全に独自のGUIを実装し、MASのクラフティングロジックのみを利用する。

**利点:**
- より柔軟なUI設計が可能
- バックパックUIとの統合が容易

**欠点:**
- 実装コストが高い
- MASのUI更新に対応が必要

---

## 使用するAPIとクラス一覧

### Mine and Slash側

| クラス/インターフェース | パッケージ | 用途 |
|---|---|---|
| `ISalvagable` | `com.robertx22.mine_and_slash.uncommon.interfaces.data_items` | サルベージ可能アイテムの判定と結果取得 |
| `ICommonDataItem` | `com.robertx22.mine_and_slash.uncommon.interfaces.data_items` | MASアイテムの共通データアクセス |
| `ExileStack` | `com.robertx22.mine_and_slash.uncommon.utilityclasses.stack` | MASのItemStackラッパー |
| `PlayerConfigData.AutoSalvage` | `com.robertx22.mine_and_slash.capability.player.data` | プレイヤーの自動サルベージ設定 |
| `ProfessionBlockEntity` | `com.robertx22.mine_and_slash.database.data.profession` | 職業作業台のBlockEntity |
| `CraftingStationMenu` | `com.robertx22.mine_and_slash.database.data.profession.screen` | 作業台のメニュー（Container） |
| `CraftingStationScreen` | `com.robertx22.mine_and_slash.database.data.profession.screen` | 作業台のGUI |
| `Profession` | `com.robertx22.mine_and_slash.database.data.profession` | 職業データ |
| `ExileDB` | `com.robertx22.mine_and_slash.database.registrators` | データベースアクセス |

### Sophisticated Backpacks側

| クラス/インターフェース | パッケージ | 用途 |
|---|---|---|
| `UpgradeItemBase<W>` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | アップグレードアイテムの基底クラス |
| `UpgradeWrapperBase<W, I>` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | アップグレードラッパーの基底クラス |
| `ITickableUpgrade` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | tick処理を持つアップグレード用インターフェース |
| `IItemHandlerInteractionUpgrade` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | アイテムハンドラーとのインタラクション用 |
| `IStorageWrapper` | `net.p3pp3rf1y.sophisticatedcore.api` | バックパックラッパー |
| `ITrackedContentsItemHandler` | `net.p3pp3rf1y.sophisticatedcore.inventory` | アイテムハンドラー |
| `UpgradeContainerBase` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | アップグレードコンテナの基底クラス |
| `UpgradeTabBase` | `net.p3pp3rf1y.sophisticatedcore.client.gui.upgrades` | アップグレードタブの基底クラス |
| `UpgradeContainerRegistry` | `net.p3pp3rf1y.sophisticatedcore.upgrades` | アップグレードコンテナの登録 |

---

## 開発ロードマップ

### Phase 1: 基本実装（自動サルベージ）
1. ✅ プロジェクトセットアップとMOD依存関係の設定
2. ⬜ `AutoSalvageUpgradeItem`の実装
3. ⬜ `AutoSalvageUpgradeWrapper`の実装
   - `ITickableUpgrade`の実装
   - MAS `ISalvagable` APIとの連携
   - プレイヤー設定との連携
4. ⬜ アップグレードアイテムの登録とレシピ作成
5. ⬜ テスト・デバッグ

### Phase 2: 作業台アクセス機能
1. ⬜ `CraftingStationUpgradeItem`の実装
2. ⬜ 各職業用のアップグレード作成
   - Salvaging Station Upgrade
   - Gear Crafting Station Upgrade
   - Cooking Station Upgrade
3. ⬜ 仮想BlockEntityの実装
4. ⬜ GUI統合の実装
5. ⬜ テスト・デバッグ

### Phase 3: 最適化とポリッシュ
1. ⬜ パフォーマンス最適化
2. ⬜ 設定UIの追加（必要に応じて）
3. ⬜ ローカライゼーション（多言語対応）
4. ⬜ ドキュメンテーション
5. ⬜ リリース準備

---

## ビルドと開発環境

### ビルドコマンド
```bash
# ビルド
./gradlew build

# クライアント起動
./gradlew runClient

# サーバー起動
./gradlew runServer

# データ生成
./gradlew runData
```

### エンコーディング設定

**重要:** このプロジェクトでは、すべてのファイルの読み書きで **UTF-8エンコーディング** を使用してください。

#### Gradle設定
```kotlin
// build.gradle.kts
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
```

#### Javaコードでのファイル読み書き
```java
// ファイル読み込み
String content = Files.readString(Path.of("path/to/file.txt"), StandardCharsets.UTF_8);

// ファイル書き込み
Files.writeString(Path.of("path/to/file.txt"), content, StandardCharsets.UTF_8);

// BufferedReaderを使う場合
try (BufferedReader reader = Files.newBufferedReader(Path.of("path/to/file.txt"), StandardCharsets.UTF_8)) {
    // 読み込み処理
}

// BufferedWriterを使う場合
try (BufferedWriter writer = Files.newBufferedWriter(Path.of("path/to/file.txt"), StandardCharsets.UTF_8)) {
    // 書き込み処理
}
```

#### NBT/データファイル
```java
// NBT保存時（Minecraftのデータ）
// CompoundTagは内部的にUTF-8を使用するため、明示的な指定は不要
CompoundTag tag = new CompoundTag();
tag.putString("key", "日本語テキスト"); // 自動的にUTF-8で保存

// JSONファイル
// Gsonは標準でUTF-8を使用
Gson gson = new GsonBuilder().setPrettyPrinting().create();
try (Writer writer = Files.newBufferedWriter(Path.of("data.json"), StandardCharsets.UTF_8)) {
    gson.toJson(data, writer);
}
```

#### リソースファイル（lang/json）
すべての言語ファイル（`src/main/resources/assets/*/lang/*.json`）はUTF-8で保存してください。

```json
{
  "item.sophisticatedmas.auto_salvage_upgrade": "自動サルベージアップグレード",
  "item.sophisticatedmas.crafting_station_upgrade": "作業台アクセスアップグレード"
}
```

### 依存関係の追加（build.gradle.kts）
```kotlin
repositories {
    maven("https://maven.robertx22.com/releases") // Mine and Slash
    maven("https://maven.saps.dev/releases") // Sophisticated Backpacks
}

dependencies {
    // Mine and Slash
    implementation(fg.deobf("com.robertx22.mine_and_slash:Mine-and-Slash:${mcVersion}-${masVersion}"))

    // Sophisticated Backpacks
    implementation(fg.deobf("net.p3pp3rf1y.sophisticatedbackpacks:sophisticatedbackpacks-${mcVersion}:${sbpVersion}"))
}
```

---

## 参考リソース

### ドキュメント
- [Sophisticated Backpacks Wiki](https://github.com/P3pp3rF1y/SophisticatedBackpacks/wiki)
- [Mine and Slash Discord](https://discord.gg/mineandslash)
- [Forge Documentation](https://docs.minecraftforge.net/)

### コードベースの重要な参照場所

**Sophisticated Backpacks:**
- アップグレード例: `src/main/java/net/p3pp3rf1y/sophisticatedbackpacks/upgrades/`
  - `everlasting/EverlastingUpgradeItem.java` - シンプルなアップグレード
  - `inception/InceptionUpgradeItem.java` - 複雑なアップグレード
  - `anvil/AnvilUpgradeItem.java` - GUI付きアップグレード

**Mine and Slash:**
- サルベージシステム: `src/main/java/com/robertx22/mine_and_slash/`
  - `uncommon/interfaces/data_items/ISalvagable.java`
  - `capability/player/data/PlayerConfigData.java`
  - `database/data/profession/ProfessionBlockEntity.java`
- GUI実装: `src/main/java/com/robertx22/mine_and_slash/database/data/profession/screen/`

---

## トラブルシューティング

### 一般的な問題

**問題1: アップグレードが認識されない**
- `UpgradeContainerRegistry.register()`が正しく呼ばれているか確認
- `ModItems.ITEMS.register()`でアイテムが登録されているか確認

**問題2: MAS APIが見つからない**
- `build.gradle.kts`で依存関係が正しく設定されているか確認
- Mine and Slashのバージョンが一致しているか確認

**問題3: GUIが表示されない**
- クライアント側での`MenuScreens.register()`を確認
- ネットワークパケットが正しく送信されているか確認

---

## ライセンスと貢献

### ライセンス
このプロジェクトは開発中です。ライセンスは後日決定されます。

### 貢献ガイドライン
- 現在はプライベート開発中
- バグ報告やフィードバックは歓迎します

---

## 連絡先

プロジェクトに関する質問や提案は、GitHubのIssueまたはDiscordでお願いします。


# AI-DLC and Spec-Driven Development

Kiro-style Spec Driven Development implementation on AI-DLC (AI Development Life Cycle)

## Project Context

### Paths
- Steering: `.kiro/steering/`
- Specs: `.kiro/specs/`

### Steering vs Specification

**Steering** (`.kiro/steering/`) - Guide AI with project-wide rules and context
**Specs** (`.kiro/specs/`) - Formalize development process for individual features

### Active Specifications
- Check `.kiro/specs/` for active specifications
- Use `/kiro:spec-status [feature-name]` to check progress

## Development Guidelines
- Think in English, but generate responses in Japanese (思考は英語、回答の生成は日本語で行うように)

## Minimal Workflow
- Phase 0 (optional): `/kiro:steering`, `/kiro:steering-custom`
- Phase 1 (Specification):
  - `/kiro:spec-init "description"`
  - `/kiro:spec-requirements {feature}`
  - `/kiro:validate-gap {feature}` (optional: for existing codebase)
  - `/kiro:spec-design {feature} [-y]`
  - `/kiro:validate-design {feature}` (optional: design review)
  - `/kiro:spec-tasks {feature} [-y]`
- Phase 2 (Implementation): `/kiro:spec-impl {feature} [tasks]`
  - `/kiro:validate-impl {feature}` (optional: after implementation)
- Progress check: `/kiro:spec-status {feature}` (use anytime)

## Development Rules
- 3-phase approval workflow: Requirements → Design → Tasks → Implementation
- Human review required each phase; use `-y` only for intentional fast-track
- Keep steering current and verify alignment with `/kiro:spec-status`

## Steering Configuration
- Load entire `.kiro/steering/` as project memory
- Default files: `product.md`, `tech.md`, `structure.md`
- Custom files are supported (managed via `/kiro:steering-custom`)


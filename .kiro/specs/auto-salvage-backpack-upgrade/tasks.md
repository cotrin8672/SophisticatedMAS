# Implementation Plan - Auto Salvage Backpack Upgrade

## Task Overview
自動サルベージバックパックアップグレード機能の実装タスク。Mine and SlashギアをSophisticated Backpacks内で自動サルベージし、素材を安全に回収する機能を提供する。

**重要**:
- **アイテム挿入イベントベース**: tick処理ではなく`IInventoryWrapperUpgrade`インターフェースを使用してアイテム挿入時に直接処理する
- **Code Context MCPによる調査済み**: Sophisticated BackpacksとMine and Slashの実際のコードベースを詳細に調査した上で作成
- **Registrateを使用**: アイテム登録とデータ生成を簡略化するためにRegistrateライブラリを使用

---

## Implementation Tasks

### Phase 1: プロジェクト基盤とRegistrate設定

#### Task 1.1: MODエントリポイントとRegistrateインスタンスを構築する
**作業内容:**
- `SophisticatedMAS.kt`を更新
  ```kotlin
  @Mod(ModIdentity.MOD_ID)
  object SophisticatedMASMod {
      val LOGGER: Logger = LogManager.getLogger(ModIdentity.LOGGER_NAME)

      // Registrateインスタンス
      val REGISTRATE: Registrate = Registrate.create(ModIdentity.MOD_ID)

      init {
          LOGGER.info("${ModIdentity.MOD_NAME} is initializing...")
          LOGGER.info("Registrate instance created")
      }
  }
  ```

**使用API:**
- `com.tterrag.registrate.Registrate.create(String)`
- イベントバスへの登録は自動的に行われる

**参考コード:**
- `RegistrateWorktrees/1.20/src/main/java/com/tterrag/registrate/Registrate.java:10-43`

**要件:** 3.1（アップグレード登録の基盤）
**見積時間:** 30分

---

#### Task 1.2: ModItemsオブジェクトを作成してAutoSalvageUpgradeItemを登録する
**作業内容:**
- `ModItems.kt`を作成
  ```kotlin
  object ModItems {
      val AUTO_SALVAGE_UPGRADE: ItemEntry<AutoSalvageUpgradeItem> =
          SophisticatedMASMod.REGISTRATE.item("auto_salvage_upgrade") { _ ->
              AutoSalvageUpgradeItem()
          }
          .properties { props ->
              props.stacksTo(16).rarity(Rarity.UNCOMMON)
          }
          .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
          .lang("Auto Salvage Upgrade")
          .lang("ja_jp", "自動サルベージアップグレード")
          .register()

      fun init() {
          SophisticatedMASMod.LOGGER.info("Registering items...")
      }
  }
  ```
- `SophisticatedMASMod`のinitから`ModItems.init()`を呼び出す

**使用API:**
- `Registrate.item(String, Function<Item.Properties, Item>)`
- `.properties(Consumer<Item.Properties>)`
- `.tab(ResourceKey<CreativeModeTab>)`
- `.lang(String)` / `.lang(String, String)`
- `.register()` → `ItemEntry<T>`を返す

**参考コード:**
- `RegistrateWorktrees/1.20/src/test/java/com/tterrag/registrate/test/mod/TestMod.java:176-182`

**要件:** 3.1
**見積時間:** 1時間

---

#### Task 1.3: AutoSalvageUpgradeItemクラスを実装する
**作業内容:**
- `AutoSalvageUpgradeItem.kt`を作成
  ```kotlin
  class AutoSalvageUpgradeItem : UpgradeItemBase<AutoSalvageUpgradeWrapper>(
      Config.SERVER.maxUpgradesPerStorage
  ) {
      companion object {
          val TYPE: UpgradeType<AutoSalvageUpgradeWrapper> =
              UpgradeType { storageWrapper, upgrade, saveHandler ->
                  AutoSalvageUpgradeWrapper(storageWrapper, upgrade, saveHandler)
              }
      }

      override fun getType(): UpgradeType<AutoSalvageUpgradeWrapper> = TYPE

      override fun getUpgradeConflicts(): List<UpgradeConflictDefinition> = emptyList()

      override fun getMemorySettingsCategory(): MemorySettingsCategory = MemorySettingsCategory.EMPTY
  }
  ```

**使用API:**
- `net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase<W>`
- `net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType<W>`
- `net.p3pp3rf1y.sophisticatedbackpacks.Config.SERVER.maxUpgradesPerStorage`

**参考コード:**
- `SophisticatedBackpacks/src/main/java/net/p3pp3rf1y/sophisticatedbackpacks/upgrades/everlasting/EverlastingUpgradeItem.java:14-25`

**要件:** 3.1
**見積時間:** 1時間

---

### Phase 2: AutoSalvageUpgradeWrapperとInventoryラッピング

#### Task 2.1: AutoSalvageUpgradeWrapperの基本構造を作成する
**作業内容:**
- `AutoSalvageUpgradeWrapper.kt`を作成
  ```kotlin
  class AutoSalvageUpgradeWrapper(
      storageWrapper: IStorageWrapper,
      upgrade: ItemStack,
      upgradeSaveHandler: Consumer<ItemStack>
  ) : UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>(
      storageWrapper,
      upgrade,
      upgradeSaveHandler
  ), IInventoryWrapperUpgrade {

      private var wrappedInventory: ITrackedContentsItemHandler? = null

      override fun wrapInventory(inventory: ITrackedContentsItemHandler): ITrackedContentsItemHandler {
          if (wrappedInventory == null) {
              wrappedInventory = SalvagingInventoryHandler(inventory, this)
          }
          return wrappedInventory!!
      }
  }
  ```

**使用API:**
- `net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase<W, I>`
- `net.p3pp3rf1y.sophisticatedcore.upgrades.IInventoryWrapperUpgrade`
  - `ITrackedContentsItemHandler wrapInventory(ITrackedContentsItemHandler)`

**参考コード:**
- `SophisticatedBackpacks/src/main/java/net/p3pp3rf1y/sophisticatedbackpacks/backpack/wrapper/InventoryModificationHandler.java:14-33`
  - `IInventoryWrapperUpgrade`を実装したアップグレードが`wrapInventory()`でハンドラーをラップする

**要件:** 1.1, 3.1
**見積時間:** 1.5時間

---

#### Task 2.2: SalvagingInventoryHandlerクラスを実装する
**作業内容:**
- `SalvagingInventoryHandler.kt`を作成
  ```kotlin
  class SalvagingInventoryHandler(
      private val wrappedHandler: ITrackedContentsItemHandler,
      private val wrapper: AutoSalvageUpgradeWrapper
  ) : ITrackedContentsItemHandler by wrappedHandler {

      override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
          // まず通常の挿入を試行
          val remaining = wrappedHandler.insertItem(slot, stack, simulate)

          // 挿入成功（一部または全部）の場合、サルベージ判定
          if (!simulate && remaining.count < stack.count) {
              val inserted = stack.copy()
              inserted.count = stack.count - remaining.count
              wrapper.onItemInserted(slot, inserted)
          }

          return remaining
      }

      override fun insertItem(stack: ItemStack, simulate: Boolean): ItemStack {
          // スロットなし版は適切なスロットを見つけて上記を呼ぶ
          val remaining = wrappedHandler.insertItem(stack, simulate)
          // ... 同様の処理
          return remaining
      }
  }
  ```

**使用API:**
- `net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler`
  - `ItemStack insertItem(int slot, ItemStack stack, boolean simulate)`
  - `ItemStack insertItem(ItemStack stack, boolean simulate)`
- Kotlinの委譲パターン: `by wrappedHandler`

**参考コード:**
- `SophisticatedBackpacks/src/main/java/net/p3pp3rf1y/sophisticatedbackpacks/upgrades/inception/InceptionInventoryHandler.java:55-65`
  - `insertItem()`のオーバーライド例

**要件:** 1.1, 2.1
**見積時間:** 2時間

---

### Phase 3: Mine and Slash統合コンポーネント

#### Task 3.1: MasIntegrationBridgeオブジェクトを作成する
**作業内容:**
- `MasIntegrationBridge.kt`を作成
  ```kotlin
  object MasIntegrationBridge {
      /**
       * ISalvagableをロード
       * Location: Mine-And-Slash-Rework/.../ISalvagable.java:29-38
       */
      fun loadSalvagable(stack: ItemStack): ISalvagable? {
          return try {
              ISalvagable.load(stack)
          } catch (e: Exception) {
              null
          }
      }

      /**
       * ICommonDataItemをロード
       */
      fun loadCommonData(stack: ItemStack): ICommonDataItem<*>? {
          return try {
              ICommonDataItem.load(stack)
          } catch (e: Exception) {
              null
          }
      }

      /**
       * ExileStackを作成
       */
      fun createExileStack(stack: ItemStack): ExileStack {
          return ExileStack.of(stack)
      }
  }
  ```

**使用API:**
- `com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ISalvagable.load(ItemStack)`
- `com.robertx22.mine_and_slash.uncommon.interfaces.data_items.ICommonDataItem.load(ItemStack)`
- `com.robertx22.mine_and_slash.uncommon.utilityclasses.stack.ExileStack.of(ItemStack)`

**参考コード:**
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/uncommon/interfaces/data_items/ISalvagable.java:29-38`

**要件:** 1.1, 1.2, 1.3, 1.4, 2.5
**見積時間:** 1時間

---

#### Task 3.2: SalvageDeciderクラスを実装する
**作業内容:**
- `SalvageDecider.kt`を作成
  ```kotlin
  class SalvageDecider(private val player: Player) {

      /**
       * サルベージすべきかどうかを判定
       * Reference: PlayerConfigData.AutoSalvage.trySalvageOnPickup():92-119
       */
      fun shouldSalvage(stack: ItemStack): Boolean {
          // エンチャント済みアイテムは自動サルベージしない
          if (stack.isEnchanted) return false

          val exileStack = MasIntegrationBridge.createExileStack(stack)
          val commonData = MasIntegrationBridge.loadCommonData(stack) ?: return false
          val salvagable = MasIntegrationBridge.loadSalvagable(stack) ?: return false

          // サルベージ可能性チェック
          if (!salvagable.isSalvagable(exileStack)) return false

          // プレイヤー設定をチェック
          val playerConfig = Load.player(player).config.salvage

          // タイプ別設定チェック（優先）
          val typeSalvageEnabled = playerConfig.checkTypeSalvageConfig(
              commonData.salvageType,
              commonData.salvageConfigurationId
          )

          if (typeSalvageEnabled.isPresent) {
              return typeSalvageEnabled.get()
          }

          // レアリティ別設定チェック（フォールバック）
          return playerConfig.checkRaritySalvageConfig(
              commonData.salvageType,
              commonData.rarityId
          )
      }
  }
  ```

**使用API:**
- `ItemStack.isEnchanted`
- `ISalvagable.isSalvagable(ExileStack)`
- `ICommonDataItem.getSalvageType()` → `ToggleAutoSalvageRarity.SalvageType`
- `ICommonDataItem.getSalvageConfigurationId()` → `String`
- `ICommonDataItem.getRarityId()` → `String`
- `com.robertx22.mine_and_slash.uncommon.datasaving.Load.player(Player)` → `ExiledPlayerData`
- `ExiledPlayerData.config.salvage` → `PlayerConfigData.AutoSalvage`
- `PlayerConfigData.AutoSalvage.checkTypeSalvageConfig(SalvageType, String)` → `Optional<Boolean>`
- `PlayerConfigData.AutoSalvage.checkRaritySalvageConfig(SalvageType, String)` → `boolean`

**参考コード:**
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/PlayerConfigData.java:92-119`

**要件:** 1.2, 1.3, 1.4
**見積時間:** 2時間

---

#### Task 3.3: SalvageExecutorクラスを実装する
**作業内容:**
- `SalvageExecutor.kt`を作成
  ```kotlin
  class SalvageExecutor(private val player: Player) {

      /**
       * サルベージを実行して結果を返す
       */
      fun executeSalvage(stack: ItemStack): SalvageResult {
          val exileStack = MasIntegrationBridge.createExileStack(stack)
          val salvagable = MasIntegrationBridge.loadSalvagable(stack)
              ?: return SalvageResult.failure("Not salvagable")
          val commonData = MasIntegrationBridge.loadCommonData(stack)
              ?: return SalvageResult.failure("No common data")

          try {
              // サルベージ結果を取得
              val results = salvagable.getSalvageResult(exileStack)

              // 経験値を付与
              grantSalvageExperience(commonData)

              // 元のアイテムを削除
              stack.shrink(1)

              return SalvageResult.success(results)
          } catch (e: Exception) {
              return SalvageResult.failure("Salvage failed: ${e.message}")
          }
      }

      /**
       * サルベージ経験値を付与
       * Reference: PlayerConfigData.AutoSalvage.trySalvageOnPickup():125-129
       */
      private fun grantSalvageExperience(commonData: ICommonDataItem<*>) {
          val salvagingProfession = ExileDB.Professions().get("salvaging")
          if (salvagingProfession != null) {
              // 最後の引数falseはrested exp bonusを適用しないことを意味する
              Load.player(player).professions.addExp(
                  player,
                  salvagingProfession.GUID(),
                  commonData.autoSalvageExpReward,
                  false
              )
          }
      }
  }

  sealed class SalvageResult {
      data class Success(val results: List<ItemStack>) : SalvageResult()
      data class Failure(val reason: String) : SalvageResult()

      companion object {
          fun success(results: List<ItemStack>) = Success(results)
          fun failure(reason: String) = Failure(reason)
      }
  }
  ```

**使用API:**
- `ISalvagable.getSalvageResult(ExileStack)` → `List<ItemStack>`
- `ICommonDataItem.getAutoSalvageExpReward()` → `int`
- `com.robertx22.mine_and_slash.database.registrators.ExileDB.Professions()` → `DatabaseRegistry<Profession>`
- `DatabaseRegistry.get(String)` → `Profession`
- `Profession.GUID()` → `String`
- `ExiledPlayerData.professions.addExp(Player, String, int, boolean)`

**参考コード:**
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/PlayerConfigData.java:125-129`

**要件:** 2.1, 2.5
**見積時間:** 2時間

---

### Phase 4: 素材ルーティングとフィードバック

#### Task 4.1: ResultRoutingStrategyクラスを実装する
**作業内容:**
- `ResultRoutingStrategy.kt`を作成
  ```kotlin
  class ResultRoutingStrategy(
      private val inventory: ITrackedContentsItemHandler,
      private val player: Player
  ) {

      /**
       * サルベージ結果をルーティング
       * Reference: PlayerConfigData.AutoSalvage.trySalvageOnPickup():131-134
       */
      fun routeResults(results: List<ItemStack>): RoutingStatistics {
          val stats = RoutingStatistics()

          results.forEach { result ->
              when {
                  // 1. バックパックに格納を試行
                  tryInsertToBackpack(result) -> {
                      stats.toBackpack++
                  }
                  // 2. バックパックが満杯ならMASバックパックシステムを試行
                  tryInsertToMasBackpacks(result) -> {
                      stats.toMasBackpack++
                  }
                  // 3. それでも失敗ならプレイヤーインベントリへ
                  tryGiveToPlayer(result) -> {
                      stats.toPlayerInventory++
                  }
                  // 4. 全て失敗ならワールドドロップ
                  else -> {
                      dropInWorld(result)
                      stats.droppedInWorld++
                  }
              }
          }

          return stats
      }

      private fun tryInsertToBackpack(stack: ItemStack): Boolean {
          val remaining = inventory.insertItem(stack, false)
          return remaining.isEmpty
      }

      /**
       * MASのバックパックシステムに格納を試行
       * Reference: PlayerConfigData.AutoSalvage.trySalvageOnPickup():131-133
       */
      private fun tryInsertToMasBackpacks(stack: ItemStack): Boolean {
          val backpacks = Load.backpacks(player).backpacks
          return backpacks.tryAutoPickup(player, stack, false)
      }

      private fun tryGiveToPlayer(stack: ItemStack): Boolean {
          return PlayerUtils.giveItem(stack, player)
      }

      private fun dropInWorld(stack: ItemStack) {
          val itemEntity = ItemEntity(
              player.level(),
              player.x,
              player.y + 0.5,
              player.z,
              stack
          )
          itemEntity.setDefaultPickUpDelay()
          player.level().addFreshEntity(itemEntity)
      }
  }

  data class RoutingStatistics(
      var toBackpack: Int = 0,
      var toMasBackpack: Int = 0,
      var toPlayerInventory: Int = 0,
      var droppedInWorld: Int = 0
  )
  ```

**使用API:**
- `ITrackedContentsItemHandler.insertItem(ItemStack, boolean)` → `ItemStack`
- `com.robertx22.mine_and_slash.uncommon.datasaving.Load.backpacks(Player)` → `BackpacksCap`
- `BackpacksCap.getBackpacks()` → `Backpacks`
- `Backpacks.tryAutoPickup(Player, ItemStack, boolean)` → `boolean`
- `com.robertx22.mine_and_slash.uncommon.utilityclasses.PlayerUtils.giveItem(ItemStack, Player)` → `boolean`
- `net.minecraft.world.entity.item.ItemEntity`

**参考コード:**
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/PlayerConfigData.java:131-134`
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/Backpacks.java:97-125`

**要件:** 2.2, 2.3, 2.4
**見積時間:** 2.5時間

---

#### Task 4.2: FeedbackEmitterオブジェクトを実装する
**作業内容:**
- `FeedbackEmitter.kt`を作成
  ```kotlin
  object FeedbackEmitter {

      /**
       * サルベージ成功時のフィードバック
       * Reference: PlayerConfigData.AutoSalvage.trySalvageOnPickup():123
       */
      fun emitSuccessFeedback(player: Player, itemName: Component, stats: RoutingStatistics) {
          // サウンド再生（サーバー側のみ）
          if (!player.level().isClientSide) {
              SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 1.25F)
          }

          // 詳細メッセージ（オプション）
          if (stats.droppedInWorld > 0) {
              player.displayClientMessage(
                  Component.translatable("message.sophisticatedmas.salvage.overflow", itemName),
                  true
              )
          }
      }

      /**
       * サルベージ失敗時のフィードバック
       */
      fun emitFailureFeedback(player: Player, reason: String) {
          // 失敗理由をログに記録
          SophisticatedMASMod.LOGGER.warn("Salvage failed for player ${player.name.string}: $reason")
      }
  }
  ```

**使用API:**
- `com.robertx22.library_of_exile.utils.SoundUtils.playSound(Player, SoundEvent, float, float)`
- `net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP`
- `Player.displayClientMessage(Component, boolean)`
- `Component.translatable(String, Object...)`

**参考コード:**
- `Mine-And-Slash-Rework/src/main/java/com/robertx22/mine_and_slash/capability/player/data/PlayerConfigData.java:123`

**要件:** 3.3, 3.4
**見積時間:** 1時間

---

### Phase 5: onItemInserted統合メソッド

#### Task 5.1: AutoSalvageUpgradeWrapperにonItemInserted()を実装する
**作業内容:**
- `AutoSalvageUpgradeWrapper.kt`に追加
  ```kotlin
  fun onItemInserted(slot: Int, stack: ItemStack) {
      val player = storageWrapper.player ?: return

      try {
          // 判定
          val decider = SalvageDecider(player)
          if (!decider.shouldSalvage(stack)) return

          // 実行
          val executor = SalvageExecutor(player)
          val result = executor.executeSalvage(stack)

          when (result) {
              is SalvageResult.Success -> {
                  // ルーティング
                  val router = ResultRoutingStrategy(
                      storageWrapper.inventoryForUpgradeProcessing,
                      player
                  )
                  val stats = router.routeResults(result.results)

                  // フィードバック
                  FeedbackEmitter.emitSuccessFeedback(player, stack.hoverName, stats)
              }
              is SalvageResult.Failure -> {
                  FeedbackEmitter.emitFailureFeedback(player, result.reason)
              }
          }
      } catch (e: Exception) {
          SophisticatedMASMod.LOGGER.error("Error processing auto salvage for slot $slot", e)
          FeedbackEmitter.emitFailureFeedback(player, "Exception: ${e.message}")
      }
  }
  ```

**使用API:**
- `IStorageWrapper.getPlayer()` → `Player?`
- `IStorageWrapper.getInventoryForUpgradeProcessing()` → `ITrackedContentsItemHandler`

**要件:** 1.1, 2.1, 3.3
**見積時間:** 1.5時間

---

### Phase 6: リソースとデータ生成

#### Task 6.1: Registrateによる言語ファイル自動生成を確認する
**作業内容:**
- Task 1.2で既に`.lang()`メソッドで設定済み
- Registrateが自動的に言語ファイルを生成する
- 手動で言語ファイルを編集する場合:
  - `src/main/resources/assets/sophisticatedmas/lang/en_us.json`
  - `src/main/resources/assets/sophisticatedmas/lang/ja_jp.json`
- **重要:** UTF-8エンコーディングで保存する

**Registrateの自動生成内容:**
```json
{
  "item.sophisticatedmas.auto_salvage_upgrade": "Auto Salvage Upgrade"
}
```

**追加メッセージ（手動追加）:**
```json
{
  "message.sophisticatedmas.salvage.overflow": "Salvaged %s (some items dropped due to full inventory)"
}
```

**要件:** 3.3, 3.4
**見積時間:** 30分

---

#### Task 6.2: データ生成を実行する
**作業内容:**
- `./gradlew runData`を実行してRegistrateのデータ生成を実行
- 自動生成されるファイル:
  - アイテムモデル（`src/generated/resources/assets/sophisticatedmas/models/item/auto_salvage_upgrade.json`）
  - 言語ファイル（英語）
  - アイテムタグ（必要に応じて）
- 生成されたファイルを確認

**参考:**
- Registrateは`.defaultModel()`と`.defaultLang()`で自動的にデータを生成

**要件:** 自動データ生成
**見積時間:** 15分

---

### Phase 7: テストと検証

#### Task 7.1: ビルドとMODロードを検証する
**作業内容:**
- `./gradlew build`を実行してビルド成功を確認
- `./gradlew runClient`でクライアント起動を確認
- ログで以下を確認:
  - Registrateが正常に初期化されている
  - アイテムが登録されている
  - エラーがない

**要件:** ビルド成功とMODロード保証
**見積時間:** 30分

---

#### Task 7.2: 基本機能の動作確認
**作業内容:**
1. クリエイティブインベントリでアップグレードアイテムを確認
2. バックパックを入手してアップグレードを装着
3. MASギア（Common、Rare等）をバックパックに投入
4. サルベージが自動的に発動することを確認
5. サルベージ結果（素材）がバックパックに格納されることを確認
6. サウンドが再生されることを確認

**要件:** 1.1, 2.2, 3.3
**見積時間:** 1時間

---

#### Task 7.3: プレイヤー設定との連携を確認
**作業内容:**
1. MASのサルベージ設定画面を開く（`/mns player_config`）
2. レアリティフィルターを設定（例: Common ONのみ）
3. バックパックにCommonギアを投入 → サルベージされる
4. バックパックにRareギアを投入 → サルベージされない
5. タイプ別フィルター（武器/防具）を設定して動作確認
6. エンチャント済みギアが自動サルベージされないことを確認

**要件:** 1.2, 1.3, 1.4
**見積時間:** 1.5時間

---

#### Task 7.4: 素材格納のフォールバック検証
**作業内容:**
1. バックパックに空きがある状態:
   - サルベージ → バックパックに格納される
2. バックパックが満杯:
   - サルベージ → MASバックパックまたはプレイヤーインベントリに転送される
3. 両方満杯:
   - サルベージ → ワールドにドロップされる
   - オーバーフローメッセージが表示される

**要件:** 2.2, 2.3, 2.4, 3.4
**見積時間:** 1時間

---

#### Task 7.5: 経験値付与の検証
**作業内容:**
1. `/mns player_info`コマンドでプレイヤー情報を確認
2. 現在のSalvaging職業経験値を記録
3. バックパックでギアをサルベージ
4. 再度`/mns player_info`で経験値が増加していることを確認
5. 経験値量が`getAutoSalvageExpReward()`に基づいて正しいことを確認

**要件:** 2.5
**見積時間:** 30分

---

#### Task 7.6: パフォーマンス検証
**作業内容:**
1. F3デバッグ画面でTPS（Ticks Per Second）を確認
2. 大量のギア（64個）を一度にバックパックに投入
3. TPS低下がないことを確認
4. アイテム挿入時の即座処理が正しく動作することを確認
5. ログにTRACE/DEBUGレベルで処理情報が出力されることを確認

**要件:** パフォーマンス要件
**見積時間:** 30分

---

### Phase 8: 最終検証とクリーンアップ

#### Task 8.1: 全要件の充足確認
**作業内容:**
- Requirement 1の全Acceptance Criteriaを検証
  - 1.1: ギア検出が正しく動作
  - 1.2: プレイヤー設定が尊重される
  - 1.3: エンチャント済みアイテムが除外される
  - 1.4: タイプ別フィルターが動作
- Requirement 2の全Acceptance Criteriaを検証
  - 2.1: サルベージ結果が正しい
  - 2.2: バックパック格納が優先される
  - 2.3: フォールバックが動作
  - 2.4: ワールドドロップが最後の手段
  - 2.5: 経験値が正しく付与される
- Requirement 3の全Acceptance Criteriaを検証
  - 3.1: アップグレードの装着/取り外しが動作
  - 3.2: NBT永続化が動作（※今回はstateless実装のため該当なし）
  - 3.3: サウンドフィードバックが再生
  - 3.4: 通知メッセージが表示

**要件:** 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.3, 3.4
**見積時間:** 1時間

---

#### Task 8.2: コード品質確認
**作業内容:**
- Kotlinコーディング規約に準拠しているか確認
- UTF-8エンコーディングで保存されているか確認
- インポート順序が正しいか確認
  - Kotlin標準
  - Java標準
  - Forge
  - Mine and Slash
  - Sophisticated Backpacks
  - 内部パッケージ
- 未使用のインポートやコードを削除
- 適切なアクセス修飾子（private/internal/public）の使用

**要件:** コード品質保証
**見積時間:** 30分

---

## 実装上の重要な注意点

### アイテム挿入イベントベースの利点
1. **効率性**: tick処理（0.5秒ごとのポーリング）ではなく、アイテム挿入時に即座に処理
2. **正確性**: スロット変更キャッシュが不要、挿入されたアイテムを直接処理
3. **シンプル**: ステートレスな実装、NBT永続化が不要

### IInventoryWrapperUpgradeの動作原理
1. `InventoryModificationHandler`が全ての`IInventoryWrapperUpgrade`を取得
2. 各アップグレードの`wrapInventory()`を順次呼び出してハンドラーをラップ
3. 最終的なラップされたハンドラーが`getInventoryForUpgradeProcessing()`から返される
4. アイテム挿入時に`insertItem()`が呼ばれ、ラップされたハンドラーが処理

**参考コード:**
- `SophisticatedBackpacks/src/main/java/net/p3pp3rf1y/sophisticatedbackpacks/backpack/wrapper/InventoryModificationHandler.java:24-33`

### Mine and Slash API使用パターン
1. **ExileStackの使用**: MAS APIは`ItemStack`ではなく`ExileStack`を使用
2. **Load.player()**: プレイヤーのMASデータにアクセスするための標準的な方法
3. **ExileDB**: データベースアクセスのための中央ハブ
4. **経験値報酬**: `getAutoSalvageExpReward()`は通常の`getSalvageExpReward()`よりも低い値を返す

### Registrateの利点
1. **自動データ生成**: モデル、言語ファイル、レシピなどを自動生成
2. **簡潔なAPI**: Fluent APIで登録コードが読みやすい
3. **型安全**: `ItemEntry<T>`で型安全なアクセス
4. **イベントバス自動登録**: `Registrate.create()`が自動的にイベントリスナーを登録

---

## Requirements Coverage Summary

### Requirement 1: ギア検出と条件評価
- **1.1**: Task 2.1, 2.2, 3.1, 3.3, 5.1, 7.2, 8.1
- **1.2**: Task 3.1, 3.2, 5.1, 7.3, 8.1
- **1.3**: Task 3.1, 3.2, 5.1, 7.3, 8.1
- **1.4**: Task 3.1, 3.2, 5.1, 7.3, 8.1

### Requirement 2: サルベージ処理と成果物管理
- **2.1**: Task 2.2, 3.3, 5.1, 8.1
- **2.2**: Task 4.1, 7.2, 7.4, 8.1
- **2.3**: Task 4.1, 7.4, 8.1
- **2.4**: Task 4.1, 7.4, 8.1
- **2.5**: Task 3.3, 7.5, 8.1

### Requirement 3: 有効化とプレイヤーフィードバック
- **3.1**: Task 1.3, 2.1, 8.1
- **3.2**: ※今回はstateless実装のため該当なし
- **3.3**: Task 4.2, 5.1, 6.1, 7.2, 8.1
- **3.4**: Task 4.2, 6.1, 7.4, 8.1

---

## Task Size Estimates
- **Phase 1 (基盤)**: 2.5時間
- **Phase 2 (Wrapper)**: 3.5時間
- **Phase 3 (MAS統合)**: 5時間
- **Phase 4 (ルーティング/フィードバック)**: 3.5時間
- **Phase 5 (統合)**: 1.5時間
- **Phase 6 (リソース)**: 0.75時間
- **Phase 7 (テスト)**: 5時間
- **Phase 8 (最終検証)**: 1.5時間

**合計見積:** 約23時間

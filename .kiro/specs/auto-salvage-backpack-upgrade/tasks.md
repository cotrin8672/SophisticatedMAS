# Implementation Plan

## Task Overview
自動サルベージバックパックアップグレード機能の実装タスク。Mine and SlashギアをSophisticated Backpacks内で自動サルベージし、素材を安全に回収する機能を提供する。

**重要**: このタスクは、Sophisticated BackpacksとMine and Slashの実際のコードベースを確認した上で作成されています。

## Implementation Tasks

- [ ] 1. プロジェクト構造とMODエントリポイントを構築する
  - MODエントリポイントクラス（`SophisticatedMAS.kt`）を作成する
  - MOD IDを`sophisticatedmas`として定義する
  - `@Mod`アノテーションでForgeに登録する
  - ロガーを初期化する（`LogManager.getLogger()`）
  - _Requirements: すべての要件実装に必要な基盤_

- [ ] 2. アイテム登録システムを実装する
- [ ] 2.1 ModItemsクラスを作成する
  - `DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)`を使用してDeferredRegisterを作成する
  - `register(IEventBus)`メソッドを実装する
  - パブリックな静的フィールドとしてDeferredRegisterを公開する
  - _Requirements: 3.1（アップグレード登録の基盤）_
  - _参考コード: `ModItems.java` in Sophisticated Backpacks_

- [ ] 2.2 AutoSalvageUpgradeItemを登録する
  - `ModItems.ITEMS.register("auto_salvage_upgrade", () -> new AutoSalvageUpgradeItem())`を追加する
  - `RegistryObject<AutoSalvageUpgradeItem>`として保持する
  - MODコンストラクタで`ModItems.register(modEventBus)`を呼び出す
  - _Requirements: 3.1_

- [ ] 3. AutoSalvageUpgradeItemクラスを実装する
- [ ] 3.1 基本構造を作成する
  - `UpgradeItemBase<AutoSalvageUpgradeWrapper>`を継承する
  - `public static final UpgradeType<AutoSalvageUpgradeWrapper> TYPE = new UpgradeType<>(AutoSalvageUpgradeWrapper::new)`を定義する
  - コンストラクタで最大装着数を設定する（`super(Config.SERVER.maxUpgradesPerStorage)`または固定値）
  - `getType()`メソッドをオーバーライドしてTYPEを返す
  - `getUpgradeConflicts()`で空のリストを返す（競合なし）
  - _Requirements: 3.1_
  - _参考コード: `EverlastingUpgradeItem.java`_

- [ ] 3.2 クリエイティブタブへの追加
  - アイテムプロパティでクリエイティブタブを設定する
  - Sophisticated Backpacksのアップグレードタブに追加する
  - _Requirements: 3.1_

- [ ] 4. AutoSalvageUpgradeWrapperクラスを実装する
- [ ] 4.1 基本構造とフィールドを作成する
  - `UpgradeWrapperBase<AutoSalvageUpgradeWrapper, AutoSalvageUpgradeItem>`を継承する
  - `ITickableUpgrade`インターフェースを実装する
  - コンストラクタ: `(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler)`
  - スロット状態キャッシュ用のMap: `Map<Integer, ItemStackSnapshot>`を定義する
  - Tickカウンター: `int tickCounter = 0`を定義する
  - クールダウン定数: `private static final int TICK_INTERVAL = 10`（0.5秒）
  - _Requirements: 1.1, 3.1, 3.2_
  - _参考コード: `AnvilUpgradeWrapper.java`, `BackpackItem.java`（tick呼び出し）_

- [ ] 4.2 NBT永続化を実装する
  - コンストラクタでNBTからデータを復元する
  - `NBTHelper.getCompound(upgrade, "smas_cache").ifPresent(tag -> ...)`を使用
  - `NBTHelper.getInt(upgrade, "smas_tick_counter").ifPresent(t -> tickCounter = t)`を使用
  - `save()`メソッド内で`upgrade.addTagElement("smas_cache", serializeCache())`を呼び出す
  - `upgrade.addTagElement("smas_tick_counter", IntTag.valueOf(tickCounter))`を呼び出す
  - キャッシュのシリアライズ/デシリアライズメソッドを実装する
  - _Requirements: 3.1, 3.2_
  - _参考コード: `AnvilUpgradeWrapper.java`のNBT処理_

- [ ] 4.3 ItemStackSnapshotクラスを実装する
  - アイテムタイプ、カウント、NBTハッシュを保持するデータクラスを作成する
  - `equals()`と`hashCode()`を実装して変更検出に使用する
  - NBTへのシリアライズ/デシリアライズメソッドを実装する
  - _Requirements: 1.1, 2.1_

- [ ] 5. Tick処理とスロット変更検出を実装する
- [ ] 5.1 tick()メソッドの基本構造を実装する
  - `tick(Entity entity, Level level, BlockPos pos)`をオーバーライドする
  - エンティティがPlayerでない場合は即座にreturnする
  - `level.isClientSide`の場合は即座にreturnする（サーバー側のみ処理）
  - Tickカウンターをインクリメントし、`TICK_INTERVAL`で割った余りが0でない場合はreturnする
  - _Requirements: 1.1_
  - _参考コード: BackpackItemのonArmorTick()でのITickableUpgrade呼び出し_

- [ ] 5.2 スロット変更検出ロジックを実装する
  - `ITrackedContentsItemHandler inventory = storageWrapper.getInventoryHandler()`を取得する
  - 全スロットをループして現在のスナップショットを作成する
  - キャッシュと現在のスナップショットを比較して変更されたスロットを特定する
  - 変更されたスロットのみをサルベージ候補リストに追加する
  - キャッシュを更新する
  - _Requirements: 1.1, 2.1_

- [ ] 6. MAS統合ブリッジを実装する
- [ ] 6.1 MasIntegrationBridgeクラスを作成する
  - `ISalvagable.load(ItemStack)`を呼び出してサルベージ可能データを取得する
  - `ICommonDataItem.load(ItemStack)`を呼び出してMASアイテムデータを取得する
  - `ExileStack.of(ItemStack)`を使用してExileStackを作成する
  - null安全のためのOptionalラッパーメソッドを提供する
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.5_
  - _参考コード: `ISalvagable.java`, `ICommonDataItem.java`_

- [ ] 6.2 プレイヤー設定チェックを実装する
  - `Load.player(player).config.salvage`を取得する
  - `checkTypeSalvageConfig(data.getSalvageType(), data.getSalvageConfigurationId())`を呼び出す
  - `checkRaritySalvageConfig(data.getSalvageType(), data.getRarityId())`を呼び出す
  - 設定に基づいてサルベージすべきかどうかをboolean値で返す
  - エンチャント済みアイテムは自動サルベージしない（`stack.isEnchanted()`でチェック）
  - _Requirements: 1.2, 1.3_
  - _参考コード: `PlayerConfigData.AutoSalvage.trySalvageOnPickup()`_

- [ ] 6.3 サルベージ実行メソッドを実装する
  - `ISalvagable.getSalvageResult(ExileStack)`を呼び出してサルベージ結果を取得する
  - `List<ItemStack>`として結果を返す
  - サルベージ不可能な場合は空のリストを返す
  - _Requirements: 1.1, 2.1_

- [ ] 6.4 経験値付与メソッドを実装する
  - `ExileDB.Professions().get("salvaging")`でProfessionを取得する
  - `Load.player(player).professions.addExp(player, profession.GUID(), data.getAutoSalvageExpReward(), false)`を呼び出す
  - 最後の引数`false`はrested exp bonusを適用しないことを意味する
  - _Requirements: 2.5_
  - _参考コード: `PlayerConfigData.AutoSalvage.trySalvageOnPickup()`, `ProfessionBlockEntity.addExp()`_

- [ ] 7. サルベージ処理コアロジックを実装する
- [ ] 7.1 processCandidates()メソッドを実装する
  - 変更されたスロットの候補リストを受け取る
  - 各候補に対してMasIntegrationBridgeを使用してサルベージ判定を行う
  - サルベージすべきアイテムのリストを作成する
  - サルベージ成功/失敗の統計情報を集約する
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1_

- [ ] 7.2 executeSalvage()メソッドを実装する
  - MasIntegrationBridgeを使用してサルベージ結果を取得する
  - 元のアイテムスタックを削除する（`stack.shrink(stack.getCount())`）
  - サルベージ結果をバックパックに格納する（次のタスク）
  - 経験値を付与する
  - サルベージ成功を記録する
  - _Requirements: 2.1, 2.5_

- [ ] 8. 素材ルーティング戦略を実装する
- [ ] 8.1 ResultRoutingStrategyクラスを作成する
  - バックパックへの挿入を試行する: `inventory.insertItem(slot, stack, false)`
  - バックパック満杯の場合はプレイヤーインベントリへ: `PlayerUtils.giveItem(stack, player)`
  - 両方満杯の場合はワールドドロップ: `ItemEntity`を作成してスポーン
  - 各フォールバックの成功/失敗を記録する
  - _Requirements: 2.2, 2.3, 2.4_
  - _参考コード: `PlayerConfigData.AutoSalvage.trySalvageOnPickup()`のバックパック統合_

- [ ] 8.2 ルーティング結果の統計情報を返す
  - バックパックに格納されたアイテム数
  - プレイヤーインベントリに格納されたアイテム数
  - ワールドにドロップされたアイテム数
  - 格納失敗したアイテム数（あれば）
  - _Requirements: 2.2, 2.3, 2.4_

- [ ] 9. フィードバックシステムを実装する
- [ ] 9.1 FeedbackEmitterクラスを作成する
  - サーバー側でのみ実行される保証を実装する（`!level.isClientSide`チェック）
  - サルベージ成功時のサウンド再生: `SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 1.25F)`
  - サルベージ失敗時のメッセージ通知: `player.sendSystemMessage(Component)`
  - 失敗理由の分類（バックパック満杯、設定により除外、など）
  - _Requirements: 3.3, 3.4_
  - _参考コード: `PlayerConfigData.AutoSalvage.trySalvageOnPickup()`のサウンド再生_

- [ ] 9.2 通知メッセージの多言語対応
  - 言語ファイル用のキーを定義する
  - 英語メッセージを作成する
  - 日本語メッセージを作成する（UTF-8エンコーディング）
  - _Requirements: 3.3, 3.4_

- [ ] 10. tick()メソッドで全コンポーネントを統合する
- [ ] 10.1 スロット変更検出からサルベージ実行までの統合
  - スロット変更を検出する
  - 候補リストを作成する
  - MasIntegrationBridgeで判定する
  - サルベージを実行する
  - 素材をルーティングする
  - フィードバックを発行する
  - キャッシュを更新する
  - _Requirements: 1.1, 2.1, 3.3_

- [ ] 10.2 例外処理を実装する
  - MAS API呼び出しの例外をキャッチする
  - ログ出力する（ERROR/WARNレベル）
  - プレイヤーにエラーメッセージを送信する（必要に応じて）
  - スタックトレースをログに記録する
  - _Requirements: すべての要件の堅牢性向上_

- [ ] 11. 言語ファイルとリソースを整備する
- [ ] 11.1 英語言語ファイルを作成する
  - `src/main/resources/assets/sophisticatedmas/lang/en_us.json`を作成する
  - アップグレード名: `"item.sophisticatedmas.auto_salvage_upgrade": "Auto Salvage Upgrade"`
  - 成功メッセージ、失敗メッセージのキーと値を定義する
  - UTF-8エンコーディングで保存する
  - _Requirements: 3.3, 3.4_

- [ ] 11.2 日本語言語ファイルを作成する
  - `src/main/resources/assets/sophisticatedmas/lang/ja_jp.json`を作成する
  - アップグレード名: `"item.sophisticatedmas.auto_salvage_upgrade": "自動サルベージアップグレード"`
  - 成功メッセージ、失敗メッセージを日本語で定義する
  - UTF-8エンコーディングで保存する
  - _Requirements: 3.3, 3.4_

- [ ] 11.3 アイテムテクスチャを作成する（オプション）
  - 16x16 PNGテクスチャを作成する
  - `src/main/resources/assets/sophisticatedmas/textures/item/auto_salvage_upgrade.png`に配置する
  - アイテムモデルJSONを作成する（必要に応じて）
  - _Requirements: 視覚的なユーザー体験向上_

- [ ] 12. 統合テストと動作検証を行う
- [ ] 12.1 ビルドとMODロードを検証する
  - `gradlew build`が成功することを確認する
  - 生成されたJARファイルを確認する
  - `gradlew runClient`でMODが正常にロードされることを確認する
  - ログに初期化エラーがないことを確認する
  - _Requirements: ビルド成功とMODロード保証_

- [ ] 12.2 基本機能の動作確認
  - クリエイティブインベントリでアップグレードアイテムを確認する
  - バックパックにアップグレードを装着する
  - MASギアを投入してサルベージが発動することを確認する
  - サルベージ結果がバックパックに格納されることを確認する
  - _Requirements: 1.1, 2.2_

- [ ] 12.3 プレイヤー設定との連携を確認する
  - MASのサルベージ設定画面でレアリティフィルターを設定する
  - タイプ別フィルター（武器/防具/その他）を設定する
  - 設定が正しく尊重されることを確認する
  - エンチャント済みアイテムがサルベージされないことを確認する
  - _Requirements: 1.2, 1.3, 1.4_

- [ ] 12.4 素材格納とフォールバックを検証する
  - バックパックに空きがある状態で素材格納を確認する
  - バックパックが満杯の状態でプレイヤーインベントリへの転送を確認する
  - 両方満杯の状態でワールドドロップを確認する
  - 各フォールバック時の通知が表示されることを確認する
  - _Requirements: 2.2, 2.3, 2.4, 3.4_

- [ ] 12.5 経験値付与とフィードバックを検証する
  - サルベージ時にMAS経験値が付与されることを確認する（`/mns player_info`コマンドで確認）
  - サルベージ成功時のサウンドが再生されることを確認する
  - サルベージ失敗時のメッセージが表示されることを確認する
  - アップグレード取り外し時に処理が停止することを確認する
  - _Requirements: 2.5, 3.1, 3.3, 3.4_

- [ ] 13. パフォーマンス最適化を検証する
- [ ] 13.1 Tick処理の最適化を確認する
  - 10tick間隔のクールダウンが正しく動作することを確認する
  - スロット変更がない場合は処理をスキップすることを確認する
  - 大量アイテム投入時の処理時間を計測する（F3デバッグ画面でTPS確認）
  - 必要に応じてバッチ分割を実装する（1tick当たり最大処理数を制限）
  - _Requirements: パフォーマンス要件_

- [ ] 13.2 ログ出力を最適化する
  - TRACE: スロット変更検出、処理候補数
  - DEBUG: サルベージ判定結果
  - INFO: サルベージ成功件数、素材格納結果
  - WARN: 失敗理由、例外
  - ERROR: クリティカルエラー
  - _Requirements: 保守性とデバッグ効率向上_

- [ ] 14. 最終検証とクリーンアップを実行する
- [ ] 14.1 全要件の充足を確認する
  - Requirement 1（ギア検出と条件評価）の全Acceptance Criteriaを検証する
  - Requirement 2（サルベージ処理と成果物管理）の全Acceptance Criteriaを検証する
  - Requirement 3（有効化とプレイヤーフィードバック）の全Acceptance Criteriaを検証する
  - 各要件に対するトレーサビリティを確認する
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4_

- [ ] 14.2 コード品質を確認する
  - Kotlinコーディング規約に準拠しているか確認する
  - UTF-8エンコーディングで保存されているか確認する
  - インポート順序が正しいか確認する（Kotlin標準 → Java標準 → Forge → 外部ライブラリ → 内部パッケージ）
  - 未使用のインポートやコードを削除する
  - 適切なアクセス修飾子が使用されているか確認する
  - _Requirements: コード品質保証_

- [ ] 14.3 ドキュメンテーションを整備する
  - 各クラスの役割をKDocコメントで記述する
  - 複雑なロジックにインラインコメントを追加する
  - README.mdに機能説明と使用方法を記載する（必要に応じて）
  - _Requirements: 保守性向上_

## Requirements Coverage Summary

### Requirement 1: ギア検出と条件評価
- **1.1**: タスク 4.1, 4.3, 5.1, 5.2, 6.1, 6.3, 7.1, 10.1, 12.2, 14.1
- **1.2**: タスク 6.1, 6.2, 7.1, 12.3, 14.1
- **1.3**: タスク 6.1, 6.2, 7.1, 12.3, 14.1
- **1.4**: タスク 6.1, 6.2, 7.1, 12.3, 14.1

### Requirement 2: サルベージ処理と成果物管理
- **2.1**: タスク 4.3, 5.2, 6.1, 6.3, 7.1, 7.2, 10.1, 14.1
- **2.2**: タスク 8.1, 8.2, 12.2, 12.4, 14.1
- **2.3**: タスク 8.1, 8.2, 12.4, 14.1
- **2.4**: タスク 8.1, 8.2, 12.4, 14.1
- **2.5**: タスク 6.4, 7.2, 12.5, 14.1

### Requirement 3: 有効化とプレイヤーフィードバック
- **3.1**: タスク 3.1, 3.2, 4.1, 4.2, 12.5, 14.1
- **3.2**: タスク 4.1, 4.2, 14.1
- **3.3**: タスク 9.1, 9.2, 10.1, 11.1, 11.2, 12.5, 14.1
- **3.4**: タスク 9.1, 9.2, 11.1, 11.2, 12.4, 12.5, 14.1

## 実装上の重要な注意点

### DeferredRegister vs Registrate
- **実装パターン**: Sophisticated Backpacksの実際のコードベースは`DeferredRegister`を使用しています
- **理由**: build.gradle.ktsにRegistrateが追加されましたが、Sophisticated Backpacksとの互換性のため、DeferredRegisterパターンに従うことを推奨します
- **代替案**: Registrateを使用する場合は、UpgradeTypeの登録とUpgradeContainerRegistryへの登録が正しく動作することを確認する必要があります

### Mine and Slash API使用パターン
1. **ExileStackの使用**: MAS APIは`ItemStack`ではなく`ExileStack`を使用します
2. **Load.player()**: プレイヤーのMASデータにアクセスするための標準的な方法
3. **ExileDB**: データベースアクセスのための中央ハブ
4. **経験値報酬**: `getAutoSalvageExpReward()`は通常の`getSalvageExpReward()`の1/10の値を返します

### Tick処理の最適化
- **間隔**: 10tick（0.5秒）ごとに処理
- **変更検出**: スロット状態キャッシュとの比較で変更されたスロットのみ処理
- **サーバー側のみ**: `level.isClientSide`チェックで確実にサーバー側のみで実行

### NBT永続化のキープレフィックス
- **プレフィックス**: `smas_`を使用して他のアップグレードとの衝突を回避
- **例**: `smas_cache`, `smas_tick_counter`

## Task Size Estimates
- **Major Task 1-3**: 2-3時間（基盤・アイテム登録）
- **Major Task 4-7**: 6-8時間（Wrapper・MAS統合・コアロジック）
- **Major Task 8-10**: 3-4時間（素材ルーティング・フィードバック・統合）
- **Major Task 11**: 1-2時間（リソースファイル）
- **Major Task 12-14**: 3-4時間（テスト・最適化・最終検証）

**合計見積**: 約16-22時間

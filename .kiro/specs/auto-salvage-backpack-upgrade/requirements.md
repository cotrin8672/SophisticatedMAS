# Requirements Document

## Introduction
Mine and SlashとSophisticated Backpacksの連携により、バックパックへ追加されたMine and Slashギアをプレイヤーの自動サルベージ設定に基づいて処理し、素材回収とインベントリ管理を自動化するための要件を定義する。

## Requirements

### Requirement 1: ギア検出と条件評価
**Objective:** As a MASプレイヤー, I want バックパック内のMine and Slashギアが設定に従って判定される, so that 不要なギアを手動整理せずに済む

#### Acceptance Criteria
1. WHEN バックパックの収納ハンドラーがMine and Slashギアを新規に受け取る THEN Auto Salvageアップグレード SHALL そのギアがサルベージ対象かどうかを評価する。
2. IF プレイヤーの自動サルベージ設定が対象ギアの分類またはレアリティを除外している THEN Auto Salvageアップグレード SHALL ギアを変更せずに保持する。
3. WHEN アイテムがMine and Slash以外のアイテムである THEN Auto Salvageアップグレード SHALL そのアイテムにサルベージ処理を適用しない。
4. WHEN アイテムがMine and Slashのサルベージ禁止状態である THEN Auto Salvageアップグレード SHALL そのアイテムにサルベージ処理を適用しない。

### Requirement 2: サルベージ処理と成果物管理
**Objective:** As a 収納効率を高めたいプレイヤー, I want サルベージ結果が安全に回収される, so that 必要な素材を取りこぼさない

#### Acceptance Criteria
1. WHEN Auto Salvageアップグレードがギアをサルベージ対象と判定する THEN Auto Salvageアップグレード SHALL サルベージ処理の完了時に元のギアスタックを完全に削除する。
2. WHEN サルベージ結果が生成される THEN Auto Salvageアップグレード SHALL 生成された素材をバックパックの空きスロットへ優先的に格納する。
3. IF バックパックに十分な空きがない THEN Auto Salvageアップグレード SHALL 生成された素材をプレイヤーのメインインベントリに移送する。
4. WHEN バックパックとプレイヤーインベントリの両方が満杯で素材を格納できない THEN Auto Salvageアップグレード SHALL 素材の消失を防ぐために結果をワールドへドロップする。
5. WHEN サルベージ処理が完了する THEN Auto Salvageアップグレード SHALL Mine and Slashのサルベージ経験値付与をトリガーする。

### Requirement 3: 有効化とプレイヤーフィードバック
**Objective:** As a プレイヤー, I want アップグレードの動作状況が明確になる, so that サルベージ結果を安心して利用できる

#### Acceptance Criteria
1. WHEN プレイヤーがAuto Salvageアップグレードをバックパックから取り外す THEN Auto Salvageアップグレード SHALL 即座にサルベージ処理を停止する。
2. IF Auto Salvageアップグレードがプレイヤーによって無効化されている THEN Auto Salvageアップグレード SHALL サルベージ対象の評価と処理を行わない。
3. WHEN サルベージ処理が成功する THEN Auto Salvageアップグレード SHALL プレイヤーに可聴または視覚フィードバックで完了を通知する。
4. WHEN バックパックとプレイヤーインベントリが満杯でサルベージ結果を格納できない THEN Auto Salvageアップグレード SHALL 失敗理由をプレイヤーに通知する。

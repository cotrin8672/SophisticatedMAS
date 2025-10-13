# Technology Stack

## Architecture

**アドオンMODアーキテクチャ**: 2つの既存MODのAPIを橋渡しする中間層として設計。
- Mine and Slash: サルベージAPI (`ISalvagable`, `PlayerConfigData`) とGUI/Container システム
- Sophisticated Backpacks: アップグレードシステム (`UpgradeWrapperBase`, `ITickableUpgrade`)

## Core Technologies

- **Language**: Kotlin (primary) + Java interop
- **Platform**: Minecraft Forge 1.20.1 (version 47.1.3)
- **Runtime**: Java 17
- **Build System**: Gradle (Kotlin DSL) with ModDevGradle Legacy

## Key Dependencies

### MOD Dependencies
- **Mine and Slash** (`maven.modrinth:mine-and-slash:6.3.7`)
  - RPGシステム、サルベージ機構、職業システム
- **Sophisticated Backpacks** (`maven.modrinth:sophisticated-backpacks:1.20.1-3.24.9.1391`)
  - アップグレードシステム、バックパックAPI
- **Sophisticated Core** (`maven.modrinth:sophisticated-core:1.20.1-1.2.105.1230`)
  - 共通アップグレード基盤
- **Kotlin for Forge** (`4.11.0`)
  - Kotlin言語サポート

### Build Tools
- **MixinExtras**: コードインジェクション（高度な機能で使用予定）
- **ModPublisher**: CurseForge/Modrinth自動公開

## Development Standards

### Language Conventions
- **Kotlin優先**: 新規コードはKotlinで記述（簡潔性、null安全性）
- **Java互換性**: Forge/MOD APIとの相互運用性を維持
- **UTF-8エンコーディング**: すべてのファイル（ソース、リソース、JSON）でUTF-8を強制

### Forge Patterns
- **@Mod annotation**: メインMODクラスに必須
- **DeferredRegister**: アイテム/ブロック/メニューの遅延登録
- **EventBusSubscriber**: Forgeイベントリスナー登録
- **Capability System**: プレイヤーデータアクセス（MAS `Load.player(player).config`）

### API Integration Principles
1. **依存MODのAPIを直接使用** - 内部実装を模倣せず、公開インターフェースに依存
2. **設定の尊重** - プレイヤーの既存設定（自動サルベージルール）を上書きしない
3. **フォールバック設計** - 依存MODの機能が無効でも、クラッシュしない

## Development Environment

### Required Tools
- JDK 17 (toolchain自動ダウンロード)
- Gradle 8.x (wrapper使用)
- IntelliJ IDEA推奨（Kotlin統合）

### Common Commands
```bash
# クライアント起動（テストプレイ）
./gradlew runClient

# サーバー起動（マルチテスト）
./gradlew runServer

# データジェネレーター（レシピ/タグ/言語ファイル）
./gradlew runData

# ビルド（配布用JAR生成）
./gradlew build

# 依存関係リフレッシュ（問題解決時）
./gradlew --refresh-dependencies
```

### MCP インデックス統合
**Code Context MCP** を活用し、依存MODのコードベースを検索：
```bash
# Mine and Slash (C:\Users\gummy\IdeaProjects\Mine-And-Slash-Rework)
# Indexed: 1403 files, 9500 chunks

# Sophisticated Backpacks (C:\Users\gummy\IdeaProjects\SophisticatedBackpacks)
# Indexed: 203 files, 1816 chunks
```

## Key Technical Decisions

### アップグレードシステム採用理由
Sophisticated Backpacksの既存アップグレードアーキテクチャ（Item/Wrapper/Container分離）を活用することで、GUIやインベントリ管理を再実装せずに済む。

### 仮想BlockEntity戦略
作業台アクセス機能では、MASの `ProfessionBlockEntity` を継承した仮想エンティティを作成し、バックパックインベントリをラップ。既存のMAS GUI/Container システムをそのまま再利用。

### Tick処理の最適化
自動サルベージは20tick（1秒）間隔で実行し、サーバー負荷を最小化。バックパックのアイテム変更検出には `ITrackedContentsItemHandler` を使用。

---
_created_at: 2025-01-13_

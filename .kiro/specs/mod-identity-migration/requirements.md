# Requirements Document

## Project Description (Input)
現在まだテンプレート用のメタデータやメインクラス、MOD IDなどのままだから、これらをとりあえずこのプロジェクト用にマイグレーションしていきたい。MOD NAME: Sophisticated MAS、MOD ID: sophisticatedmas

## Introduction
Sophisticated MASはテンプレート由来の識別情報を継承した状態から出発しており、配布や連携先で正しいMOD名・MOD IDを提供できるようにプロジェクト固有のメタデータへ統一する必要がある。本フェーズではエントリーポイント、登録設定、リソース命名を全面的にSophisticated MAS用へ揃えることに注力する。

## Requirements

### Requirement 1: MOD識別子とメタデータの統一
**Objective:** MOD運用担当として、配布メタデータに一貫したMOD識別子を適用したい。そうすることで公開チャネル間で情報が混在しなくなる。

#### Acceptance Criteria
1. WHEN Forgeがmods.tomlを評価すると THEN Sophisticated MAS Mod SHALL modIdに"sophisticatedmas"を設定し表示名として"Sophisticated MAS"を宣言する。
2. IF ビルドスクリプトまたはパックメタデータがMOD識別情報を参照する THEN Sophisticated MAS Mod SHALL "sophisticatedmas"ネームスペースと"Sophisticated MAS"表示名を提供する。
3. WHERE 公開用ドキュメントや説明欄がMODメタデータに含まれる THE Sophisticated MAS Mod SHALL テンプレート固有の名称を排除してプロジェクト固有の情報へ置き換える。
4. WHEN CurseForgeやModrinth向けの公開設定をエクスポートすると THEN Sophisticated MAS Mod SHALL 最新のプロジェクト名・IDをメタデータへ反映する。

### Requirement 2: エントリーポイントとパッケージの整備
**Objective:** 開発者として、Forge初期化で正しいメインクラスとパッケージ構成が呼び出されるようにしたい。そうすることで実行時にテンプレート残存による不整合を避けられる。

#### Acceptance Criteria
1. WHEN ForgeがMOD初期化イベントをディスパッチすると THEN Sophisticated MAS Mod SHALL io.github.cotrin8672.sophisticatedmasパッケージ配下のメインクラスを登録する。
2. IF ソースツリー内にテンプレート由来のパッケージ名やMOD IDを含むクラスが残存する THEN Sophisticated MAS Mod SHALL リファクタリングして新しいパッケージ構成と識別子へ更新する。
3. WHEN DeferredRegisterや同等の登録オブジェクトを初期化すると THEN Sophisticated MAS Mod SHALL "sophisticatedmas"ネームスペースで登録する。
4. WHERE ロガー名やリソースキーがハードコードされた識別子を使用する THE Sophisticated MAS Mod SHALL 新しいMOD IDに整合する値を採用する。

### Requirement 3: リソースおよび設定ファイルのネームスペース整合
**Objective:** クリエイティブ担当として、リソースと設定の全てが新しいネームスペースに揃っている状態を確保したい。そうすることで言語ファイルやテクスチャ参照の欠落を防げる。

#### Acceptance Criteria
1. WHEN assetsディレクトリに言語またはテクスチャファイルを配置すると THEN Sophisticated MAS Mod SHALL ディレクトリ階層をassets/sophisticatedmasに統一する。
2. IF データパックまたはリソース内にテンプレートネームスペースの参照が検出される THEN Sophisticated MAS Mod SHALL それらを"sophisticatedmas"ネームスペースへ置換する。
3. WHEN JSON設定やロケールファイルがロードされる THEN Sophisticated MAS Mod SHALL 翻訳キーのプレフィックスとして"sophisticatedmas"を使用する。
4. WHILE データ生成タスクが実行される THE Sophisticated MAS Mod SHALL 出力物に新しいネームスペースを付与しテンプレート識別子を生成しない。

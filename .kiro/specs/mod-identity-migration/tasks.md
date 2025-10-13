# Implementation Plan

- [x] 1. ビルドメタデータとプロパティを統一する
  - gradle.propertiesのmodId、modName、modGroupIdをSophisticated MAS向けの値に書き換える
  - mods.tomlテンプレートが正しいプレースホルダーを参照するよう確認する
  - ModPublisherの設定が更新後のプロパティを参照できることを検証する
  - generateModMetadataタスクを実行して生成物が新しいMOD IDを含むことを確認する
  - _Requirements: 1.1, 1.2_

- [x] 1.1 ビルド定義の識別子を更新する
  - gradle.propertiesのmodId/modName/modGroupId/modVersionを最新値へ置換する
  - archivesBaseNameやビルド成果物名が新しいMOD IDで生成されることを確認する
  - mods.tomlテンプレートの置換キー（${modId}など）が適切に定義されていることを検証する
  - _Requirements: 1.1, 1.2_

- [x] 1.2 公開用メタデータを更新する
  - mods.toml内の説明文とクレジット表記をテンプレート固有の内容から置き換える
  - ModPublisher設定（publishModrinth/publishCurseForge）で表示名と依存宣言を同期させる
  - 公開設定のプレビュービルドを実行し、エクスポート結果が新しい識別子を反映することを確認する
  - _Requirements: 1.3, 1.4_

- [x] 2. 識別子の単一情報源を定義する
  - ModIdentityオブジェクトを作成してMOD ID・表示名・ロガー名を定数として定義する
  - Kotlinエントリーポイントが@ModアノテーションでModIdentity.MOD_IDを参照するよう設定する
  - 将来のDeferredRegisterやロガー初期化でModIdentity定数を利用できる基盤を整える
  - _Requirements: 2.2, 2.3_

- [x] 2.1 ModIdentityオブジェクトを実装する
  - init/ModIdentity.ktを新規作成し、MOD_ID/MOD_NAME/LOGGER_NAMEを定義する
  - gradle.propertiesの値と整合するよう定数値を設定する
  - Forgeの命名規則（小文字英数字とアンダースコア）に準拠することを確認する
  - _Requirements: 2.2, 2.3_

- [x] 2.2 エントリーポイントを移行する
  - ExampleMod.ktをSophisticatedMASMod.ktにリネームし、パッケージをio.github.cotrin8672.sophisticatedmasへ移動する
  - @ModアノテーションをModIdentity.MOD_IDを参照するよう修正する
  - Loggerなどの初期化コードがModIdentity.LOGGER_NAMEを利用するよう調整する
  - _Requirements: 2.1, 2.4_

- [x] 3. リソースのネームスペースを統一する
  - assets/examplemodディレクトリをassets/sophisticatedmasへリネームする
  - langファイル内の翻訳キーをsophisticatedmasプレフィックスへ置換する
  - テクスチャやモデル参照が新しいネームスペースを使用することを確認する
  - _Requirements: 3.1, 3.3_

- [x] 3.1 リソースディレクトリをリネームする
  - assets配下のフォルダー名をexamplemodからsophisticatedmasへ変更する
  - langファイルのパスと内容を新しいネームスペースに合わせる
  - テクスチャやリソース参照が更新後のパスで正しく解決されることを簡易検証する
  - _Requirements: 3.1, 3.3_

- [x] 3.2 データ生成の整合性を確保する
  - Data Generator関連設定やJSON出力がsophisticatedmasネームスペースを使用することを確認する
  - テンプレート識別子を参照している箇所がないか検索して置換する
  - runDataタスクを実行し、出力ファイルが新しいネームスペースのみで構成されることを検証する
  - _Requirements: 3.2, 3.4_

- [x] 4. ビルドとランタイムの検証を完了する
  - generateModMetadataとbuildタスクを実行して生成物が新しい識別子を含むことを確認する
  - runClientでクライアントを起動し、MODリストにSophisticated MASが表示されることを検証する
  - Forge起動ログを確認し、@Mod識別子とmods.tomlの整合性をチェックする
  - _Requirements: 1.1, 1.4, 2.1, 2.4, 3.4_

- [x] 4.1 ビルドプロセスを検証する
  - gradlew generateModMetadataを実行し、生成されたmods.tomlが新しいMOD ID・名称を含むことを確認する
  - gradlew buildを実行し、成果物のJARファイル名が新しい識別子を反映していることをチェックする
  - 静的チェックやgrepで旧識別子が残存していないことを検索する
  - _Requirements: 1.1, 1.2, 3.4_

- [x] 4.2 ランタイム起動を検証する
  - gradlew runClientでクライアントを起動し、MODリスト画面にSophisticated MASが表示されることを確認する
  - Forge起動ログでmodIdとmods.tomlの整合を検証する
  - 必要に応じてModPublisherのプレビュービルドを実行し、公開メタデータが正しく更新されていることを確認する
  - _Requirements: 1.4, 2.1, 2.4_

# コードベース調査レポート

## 背景

プロジェクトの現状把握のため、全ソースコードを調査した。

## プロジェクト概要

Transform-Into-LittleMaid (TILM) は、プレイヤーをメイドさんに変身させる Minecraft 1.20.1 対応 Mod。
Architectury Loom による Fabric/Forge 両対応。LMML (LittleMaidModelLoader) に依存。

- バージョン: 5.0.0
- 総 Java ファイル数: 21
- 総コード行数: 約 614 行 (common のみ)

## プロジェクト構造

```
common/src/main/java/net/sistr/transformintolittlemaid/
├── TransformIntoLittleMaidMod.java   # MODID 定数
├── setup/
│   ├── ModSetup.java                 # ネットワーク初期化
│   └── ClientSetup.java             # クライアント初期化 (キー登録等)
├── mixin/                            # Mixin (5クラス)
│   ├── MixinPlayerEntity.java        # 変身状態・モデル管理・サイズ計算・NBT永続化
│   ├── MixinServerPlayerEntity.java  # リスポーン時のモデルコピー (サーバー)
│   ├── MixinClientPlayNetworkHandler.java  # リスポーン時のモデルコピー (クライアント)
│   ├── MixinMinecraftClient.java     # キー入力イベント発火
│   └── MixinEntityRenderDispatcher.java    # レンダラー動的切り替え
├── network/
│   ├── Networking.java               # C2S パケット登録
│   ├── TransformLittleMaidPacket.java      # 変身状態切り替え C2S
│   └── RequestSyncMultiModelPacket.java    # モデル同期要求 C2S
├── client/
│   ├── layer/KeyRegister.java        # キーバインディング登録
│   ├── manager/KeyManager.java       # キー入力処理
│   └── renderer/LittleMaidPlayerRenderer.java  # メイドさんレンダリング
├── layer/
│   ├── EventRegister.java            # イベントリスナー管理
│   └── NetworkUtil.java              # ネットワークユーティリティ
└── util/
    ├── LittleMaidTransformable.java  # 変身状態 interface
    ├── PlayerEntityCaps.java         # モデル Capabilities
    └── WaitTime.java                 # 待機時間 interface

fabric/  # TILMFabric.java (エントリーポイント)
forge/   # TransformIntoLittleMaidModForge.java (エントリーポイント)
```

## 主要システム

### 変身メカニズム

- `MixinPlayerEntity` が PlayerEntity に `IHasMultiModel`, `LittleMaidTransformable`, `WaitTime` を実装
- `DataTracker` で変身状態 (`TRANSFORMED_LITTLEMAID`) を同期
- 変身時はエンティティサイズ (幅・高さ・目の高さ) をモデルに合わせて動的変更

### ネットワークフロー

```
[クライアント] キー入力 → KeyManager.pressTransform()
    → TransformLittleMaidPacket.sendC2SPacket(boolean)
    → Architectury NetworkManager で C2S 送信
    → [サーバー] setTransformedLittleMaid_TLM() + calculateDimensions()
    → DataTracker 経由で全クライアントに自動同期
```

### レンダリング

- `MixinEntityRenderDispatcher` が変身時に `LittleMaidPlayerRenderer` を返す
- `LittleMaidPlayerRenderer` は `PlayerEntityRenderer` を継承し、内部に LMML の `MultiModelRenderer` を持つ
- 弓状態・契約状態・待機状態 (60tick 以上無動作) を `syncCaps()` で同期

### キーバインディング

| キー | 機能 |
|------|------|
| `'` (アポストロフィ) | 変身状態の切り替え |
| `[` (左ブラケット) | モデル選択画面を開く |

## 依存関係

| 依存 | 用途 |
|------|------|
| LMML (littlemaidmodelloader >= 8.0.0) | メイドさんモデル・レンダリング基盤 |
| Architectury API (>= 9.0.0) | クロスプラットフォーム API |
| Cloth Config (>= 11.0.0) | 設定 GUI (現状未使用) |
| Mod Menu (>= 7.0.0) | Mod 一覧画面統合 (現状未使用) |

## 品質面の観察

### 良い点

- Architectury による Fabric/Forge 両対応が適切に実装されている
- DataTracker による自動同期で状態管理がシンプル
- NBT 永続化・リスポーン時コピーにより UX が良い
- interface による責務分離 (`LittleMaidTransformable`, `WaitTime`)

### 改善の余地

| 項目 | 詳細 |
|------|------|
| NetworkUtil の非効率 | 全プレイヤーにブロードキャスト（コメント「めんどくさいのでプレイヤー全員」） |
| PlayerEntityCaps | カスタム Caps 登録が空（テンプレートのまま） |
| Mixin への集約 | MixinPlayerEntity にロジックが集中しており責務分散の余地あり |
| テストコード | なし |
| EventRegister | リスナー解除機能がない（追加のみ） |
| Cloth Config / Mod Menu | 依存宣言あるが未使用 |

## 結論

小規模ながらよく整理された Mod。コア機能（変身・モデル管理・レンダリング）は安定動作している。
改善するなら、MixinPlayerEntity のロジック分散と NetworkUtil の最適化が優先候補。

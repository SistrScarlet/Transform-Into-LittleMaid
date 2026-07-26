# 自分のモデル同期が届かず当たり判定がクライアント/サーバーで食い違う問題

## 背景

1.21.1 移植で失われていた「変身時のサイズ・視点調整」を復元 (`8298a46`) した直後の実機確認で、
ワールド参加直後だけ当たり判定がずれていることが発覚した。変身キーを押すと直る。

`ServerPlayerEntity` に `getBaseDimensions` の override は無く、`PlayerEntity#tick` も
`playerTick()` 経由で毎 tick 走るため、コードリーディングだけでは原因を特定できなかった。
両サイドの状態を出すデバッグビルド (`debug/dimension-sync`) を作って実測した。

## 調査結果

### 1 回目 — デフォルトモデルでは再現しない

```
[SERVER] readNbt IsChanged_TLM=true tex=default
[SERVER] tick3 ... tex=default dims=0.5x1.35 field=0.5x1.35 bbH=1.35
[CLIENT] tick3 ... tex=default dims=0.5x1.35 field=0.5x1.35 bbH=1.35
```

完全に一致していた。両サイドとも `default` を見ているのだから当然で、
**モデルがデフォルトのままでは再現しない**ことが分かった。

### 2 回目 — デフォルト以外のモデルで再現

`elsa5` に変更して入り直すと再現した。

```
[SERVER] readNbt IsChanged_TLM=true tex=e15color_elsa5
[SERVER] tick80 ... tex=e15color_elsa5 model=elsa5   dims=0.5x1.75 bbH=1.75
[CLIENT] tick80 ... tex=default        model=default dims=0.5x1.35 bbH=1.35
```

**0.4m の差**。サーバーは NBT から `elsa5` を正しく復元していて、クライアントだけが
`default` のまま取り残されていた。参加直前 (18:06:16) はクライアントも `elsa5`/1.75 だったので、
入り直した瞬間に巻き戻っている。

「サーバー側が見た目と合っていない」という体感だったが、実際は**クライアントが自分のモデルを
受け取れていなかった**。レンダリングもクライアントの `default` で行われるため、
「小さいメイドさんが表示されているのに 1.75m 分の当たり判定をサーバーが持っている」状態になる。

## 原因

`RequestSyncMultiModelPacket` は、クライアントが参加直後に自分のモデルをサーバーへ要求する。
サーバーは `SyncMultiModelPacket.sendS2CPacket(target, ...)` で返信するが、その宛先は
LMML の `PlayerList.tracking(target)` である。

この実装がプラットフォームで食い違っている。

| 実装 | 返すもの | 自分自身 |
|---|---|---|
| Fabric | `PlayerLookup.tracking()` = バニラ tracker の listener | **含まない** |
| NeoForge / Forge | `world.getPlayers()` 全員 | 含む |

バニラの `ServerChunkLoadingManager.EntityTracker#updateTrackedStatus` が
`if (player != this.entity)` で自分自身を listener から外すため、Fabric 版は自分を含まない。
Fabric API 側もこれを明記している。

> **Warning**: If the provided entity is a player, it is not guaranteed by the contract that
> said player is included in the resulting stream.

結果、**自分のモデルを要求した本人にだけ返信が届かない**。NeoForge では偶然動いていたため、
Fabric 固有の不具合に見えていた。

なお変身キーで直って見えたのは、`TransformLittleMaidPacket.transformC2S` が明示的に
`calculateDimensions()` を呼ぶためで、モデル自体は直っていない。両サイドが各自の持っている
モデルで計算し直すだけなので、実際にはサーバーが 1.75m のままずれ続けていた。

## 修正

`RequestSyncMultiModelPacket.receiveC2S` で、要求元と対象が同一プレイヤーのときに要求元へ直接も送る。

```java
SyncMultiModelPacket.sendS2CPacket(target, (IHasMultiModel) target);
if (player == target && player instanceof ServerPlayerEntity requester) {
    NetworkManager.sendToPlayer(
            requester,
            new SyncMultiModelS2CPacket(SyncMultiModelData.of(target, (IHasMultiModel) target)));
}
```

要求 — 返信の関係にある通信で返信が要求元に届かないのは、原因が何であれ塞ぐべき穴なので、
プラットフォーム差を待たずに TILM 側で対処した。NeoForge では同じ内容が二重に届くが、
`setTextureHolder` は冪等なので害はない。

検証ログ (`verify/dimension-sync`):

```
tick1  [CLIENT] tex=default        0.5x1.35   ← 要求の往復 1 tick 分だけ遅れる
tick2  [CLIENT] tex=e15color_elsa5 0.5x1.75   ← 以降ずっとサーバーと一致
```

## 残課題

LMML の `PlayerList.tracking` のプラットフォーム差は未解決のまま残っている。
Fabric は「本当に tracking 中のプレイヤー」、NeoForge/Forge は「ワールド内の全プレイヤー」で、
距離の扱いも自分自身の扱いも違う。同じ API 名で意味が違うため、これに依存するコードは
どちらかのプラットフォームで壊れる。LMML の TODO に登録済み。

## 結論・教訓

- **クロスプラットフォームのヘルパーは、名前が同じでも意味が同じとは限らない**。
  特に「tracking」はバニラの tracker の意味論を引き継ぐため、自分自身の扱いに注意する。
- 要求 — 返信型の通信は、ブロードキャスト用のヘルパーで返信してはいけない。
  宛先は要求元であって「対象を見ている人たち」ではない。
- 同期不具合は、**両サイドの同じ値を同じラベルで並べて出す**のが最短。今回は
  `tex` / `dims` / `field` / `bbH` を両サイドで出したことで、値そのものではなく
  モデルの取り違えだと 1 回のログで分かった。
- デフォルト値で試すと同期不具合は隠れる。同期の検証は**デフォルトから外した状態**で行う。

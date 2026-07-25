# TODO

## 高

- **変身時のサイズ・視点調整が機能しない(1.21.1移植の機能ロス)**: 変身してもメイドさんモデルの見た目サイズに応じた当たり判定・視点の高さが変わらない。2026-07-25 の調査で復元手段を特定済み — final 化されたのは `LivingEntity#getDimensions` であって、実質の拡張点は `getBaseDimensions(EntityPose)` (protected・非 final) に移されている。`PlayerEntity` 自身がこれを override 済み(水泳/睡眠ポーズ用)なので `@Mixin(PlayerEntity.class)` から `@Inject(method = "getBaseDimensions", ...)` が remap を通る。eyeHeight は `EntityDimensions.withEyeHeight()` で埋め込めば final な `getEyeHeight` が自動追従するため専用の注入ポイントは不要。詳細・実装時の罠は `LMML/docs/research/2026-07-25_dynamic-dimensions-1211-restore.md`
- **騎乗時の自身のYオフセット (`getHeightOffset` 相当) が失われている**: 変身状態で何かに騎乗した際の位置がずれる。2026-07-25 の調査で `Entity#getVehicleAttachmentPos(Entity)` が `getHeightOffset` の直接の後継と判明(非 final、`LivingEntity`/`PlayerEntity` とも override 無し)。既存の `getPassengerRidingPos` override と同じパターンで `@Override` を足せば復元可能。ただし 1.20.1 の加算式に対し 1.21.1 は減算式のため**符号が逆**になる点に注意
- **実機起動未検証**: ヘッドレス環境のためビルド確認のみ。クライアント起動、変身キー、モデル選択画面、防具装着、マルチプレイでの同期(リスポーン引き継ぎ含む)を実機で確認すること

## 中

- **1人称視点時にElsaモデルで自身の手(腕)が表示されない**: 変身状態で1人称視点にすると腕/手モデルが見えない。Elsaモデル固有の問題と判明(他モデルでは未発生)。モデル側の実装(パーツ欠落/レンダラー分岐等)を要調査
- `getPassengerRidingPos` オーバーライドで `getMountedYOffset` を反映しているが、実際に自分の上に乗客が乗った際の見た目が正しいか実機確認が必要(X/Z座標は計算していないので、モデルの回転等によってはズレる可能性)

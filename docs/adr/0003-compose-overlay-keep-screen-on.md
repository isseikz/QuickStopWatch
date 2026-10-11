# ADR-0003: Compose View を使ったオーバーレイと FLAG_KEEP_SCREEN_ON による常時オン

- 状態: Accepted
- 日付: 2026-10-12
- 関連: REQ-001, REQ-004, REQ-010, NFR-001

## コンテキスト
他のアプリの上に重畳表示する UI（FloatingStopwatch）の実装方式と、表示中の画面常時オン（REQ-010）の実現手段を決定する必要がある。

## 選択肢
1. **WindowManager + ComposeView + FLAG_KEEP_SCREEN_ON**:
   - 利点:
     - Compose の UI コード・テーマ・プレビューをそのままオーバーレイ描画に活用できる。
     - `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` を付与することで、`WAKE_LOCK` 権限を一切使わずに画面常時オンを実現可能（NFR-001 に合致）。
     - オーバーレイが表示されている間だけ OS レベルで自動的に常時オンが有効化され、ウィンドウ削除時に確実に解除される。
   - 欠点: Service 上で ComposeView を動かすために `LifecycleOwner` / `ViewModelStoreOwner` / `SavedStateRegistryOwner` の適切な設定（`setViewTreeLifecycleOwner` 等）が必要。
2. **従来型カスタム View または WakeLock API**:
   - 利点: ライフサイクルの設定が不要。
   - 欠点: Compose を使えない。`PowerManager.WakeLock` を使うと `WAKE_LOCK` 権限が必要になり、リークのリスクがある。

## 決定
WindowManager に `ComposeView` を追加し、`WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` および `FLAG_NOT_FOCUSABLE` を設定する方式を採用する。

## 結果
- `WAKE_LOCK` 権限なしで常時オン要件（REQ-010, NFR-001）を満たす。
- Compose の高生産性とプレビュー環境をそのまま活かすことができる。

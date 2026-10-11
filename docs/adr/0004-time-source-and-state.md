# ADR-0004: 状態管理・時刻計測モデルと SharedPreferences / DataStore

- 状態: Accepted
- 日付: 2026-10-12
- 関連: REQ-005, REQ-006, REQ-007, REQ-008, REQ-012, NFR-004, NFR-006

## コンテキスト
ストップウォッチの計測状態（待機・計測中・一時停止・上限）の管理、端末起動からの経過時間（`SystemClock.elapsedRealtime()`）に基づく高精度・省電力なタイマー処理、およびドラッグ移動した座標の永続化方法を決定する必要がある。

## 選択肢
1. **Ticker ベースの毎フレーム再計算 vs 1秒周期のコルーチンフロー + elapsedRealtime 差分**:
   - 決定: 状態モデル内部で「開始基準時刻（ベースタイム）」と「累積経過時間」を保持し、`SystemClock.elapsedRealtime()`（テスト時は抽象化した `TimeSource`）との差分を計算する。表示更新は 1 秒周期の定期更新（Ticker Flow）で行うことで、省電力要件（NFR-006）と精度要件（REQ-008, NFR-004）を両立する。
2. **座標永続化**:
   - 決定: 保存するデータはフローティング UI の (x, y) 座標のみであるため、シンプルな永続化機構（`SharedPreferences` または Jetpack DataStore）を採用する。アプリ内依存関係を増やさず軽量に動作させるため、暗黙の非同期書込が可能な DataStore または SharedPreferences を利用する。

## 決定
`TimeSource` インターフェースによる時間抽象化 + `StopwatchController` ドメインクラスで状態遷移と経過時間をカプセル化し、位置座標の永続化にはリポジトリ抽象を用意する。

## 結果
- ユニットテストで時間を自由に進めることができ（FakeTimeSource）、高カバレッジな TDD が可能になる。

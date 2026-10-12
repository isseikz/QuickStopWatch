# QuickStopWatch

[![Android CI](https://github.com/isseikz/QuickStopWatch/actions/workflows/verify.yml/badge.svg)](https://github.com/isseikz/QuickStopWatch/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

他のアプリの上に重畳して表示できる、ミニマルな円形フローティング・ストップウォッチ Android アプリです。  
クイック設定（Quick Settings）タイルから即座に表示・非表示を切り替えられます。

---

## 🌟 主な特徴

- **クイック設定タイル連携**: ステータスバーを引き下げてタイルをワンタップするだけで、どの画面からでもストップウォッチを即座に表示/非表示（リセット）。
- **フローティング表示 (Picture-in-Picture ライク)**:
  - 画面最前面に FAB サイズ（72dp）の円形 UI をオーバーレイ表示。
  - ドラッグして画面内の好きな位置へ移動可能（次回表示時も前回の位置を記憶）。
- **シンプルなワンタップ操作**:
  - タップで「計測開始」と「一時停止」を素早くトグル。
  - 触覚フィードバック（ハプティクス）による確実な操作感。
- **わかりやすい状態表現**:
  - 待機（Idle）・計測中（Running）・一時停止（Paused）の状態を Material 3 カラーで直感的に識別。
- **経過時間フォーマット**:
  - `mm:ss` 表示（60 分以降も `hh` 形式に切り替わらず `60:00`, `999:59` のように分を加算）。
- **画面常時オン (KEEP_SCREEN_ON)**:
  - オーバーレイ表示中は自動的にスリープを防止。
- **最小限の権限設計**:
  - 要求する権限は「他のアプリの上に重ねて表示（`SYSTEM_ALERT_WINDOW`）」のみ。
  - 前面サービス（Foreground Service）、WakeLock、バイブレーション（`VIBRATE`）、通知、インターネット権限（`INTERNET`）は一切不要・不使用。

---

## 📱 動作要件

- **対象 OS**: Android 12 (API レベル 31) 以上
- **Target SDK**: Android 16 (API レベル 36)
- **UI フレームワーク**: Jetpack Compose + Material 3

---

## 🚀 使い方

1. **アプリの初回起動**:
   - アプリアイコンをタップすると使い方ガイド画面が表示されます。
   - 「他のアプリの上に重ねて表示」の権限許可が案内されますので、トグルをオンにします。
2. **クイック設定タイルの追加**:
   - 端末のクイック設定パネルの編集（鉛筆アイコン）を開きます。
   - 「ストップウォッチ」（または「Stopwatch」）タイルを配置します。
3. **計測**:
   - タイルをタップするとフローティング・ストップウォッチが出現します。
   - タップで計測開始・一時停止。
   - タイルを再度タップして非表示にすると、計測がリセットされます。

---

## 🛠️ 開発・ビルド環境

- **言語**: Kotlin 2.1.10
- **ビルドツール**: Gradle 8.11.1
- **推奨 JDK**: JDK 17

### 検証コマンド

```bash
# 全検証（Spotless + Android Lint + ユニット/UI テスト + Roborazzi スクリーンショット検証 + トレーサビリティ検査）
./scripts/verify.sh

# 高速検証（テスト実行のみ）
./scripts/verify.sh --fast

# コード自動整形
./gradlew spotlessApply

# スクリーンショット記録 / 比較 (Roborazzi)
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

---

## 📐 アーキテクチャ

単一 `:app` モジュール構成のクリーンな MVVM / レイヤードアーキテクチャを採用しています。

- **`domain`**: `StopwatchEngine`（ステートマシン、タイマー計算ロジック、`FakeTimeSource` による高精度テスト）
- **`data`**: `OverlayPositionRepository`（DataStore Preferences による位置永続化）
- **`service`**:
  - `StopwatchOverlayService`: Compose View を WindowManager 経由で管理する軽量オーバーレイサービス
  - `StopwatchOverlayController`: タッチ・ドラッグ処理およびウィンドウフラグ制御
  - `QuickStopWatchTileService`: タイルの状態管理・権限ハンドリング
- **`ui`**:
  - `FloatingStopwatch`: 円形 Compose UI（セマンティクス・ハプティクス対応）
  - `UsageScreen`: 使い方説明および権限誘導画面

---

## 📄 ライセンス

MIT License

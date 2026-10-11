# テスト設計書

| 項目 | 内容 |
|---|---|
| バージョン | 1.0 |
| 状態 | IN_REVIEW |
| 入力 | requirements.md v1.0, ui-design.md v1.0, detailed-design.md v1.0 |

## 1. テスト方針（テストピラミッド）
| レベル | ツール | 場所 | 対象 | 実行タイミング |
|---|---|---|---|---|
| **Unit** | JUnit4, coroutines-test, Turbine | `app/src/test` | Domain (StopwatchEngine), Data (PositionRepo), ViewModel | L1 毎回 |
| **Compose UI** | Robolectric + compose-ui-test | `app/src/test` | FloatingStopwatch, UsageScreen | L1 毎回 |
| **Screenshot** | Roborazzi (@Preview 自動テスト) | `app/src/test/screenshots` | 見た目の回帰 | L2 / verify |
| **E2E / Journey** | Android CLI Journey | `journeys/*.xml` | ユーザーシナリオ | L4 / リリース前 |

## 2. テストケース
| ID | レベル | 対象 (DD/SCR) | 検証する要件 | 前提 (Given) | 操作 (When) | 期待結果 (Then) |
|---|---|---|---|---|---|---|
| **TC-001** | Compose UI | DD-005, SCR-002 | REQ-001 | オーバーレイが表示中 | 画面をキャプチャ | 円形ストップウォッチが表示される |
| **TC-002** | Unit | DD-007 | REQ-002 | 非表示状態 | タイルをクリック | オーバーレイ開始インテントが発行されタイルが active になる |
| **TC-003** | Unit | DD-007 | REQ-003 | 権限未許可 | タイルをクリック | 設定画面インテントが発行されオーバーレイは起動しない |
| **TC-004** | Compose UI | DD-005, SCR-002 | REQ-004 | FloatingStopwatch を描画 | サイズを取得 | 72dp の円形サイズである |
| **TC-005** | Unit | DD-003 | REQ-005 | Idle 状態 (`00:00`) | toggle() を呼ぶ | Running になり時間が進み始める |
| **TC-006** | Unit | DD-003 | REQ-005 | Running 状態 | toggle() を呼ぶ | Paused になり時間が停止する |
| **TC-007** | Unit | DD-003 | REQ-005 | Paused 状態 | toggle() を呼ぶ | Running に戻り停止した時間から再開する |
| **TC-008** | Unit | DD-003 | REQ-006 | 59分59秒経過 | 1秒進める | 表示が `60:00` になる |
| **TC-009** | Unit | DD-003 | REQ-006 | 125分7秒経過 | 表示テキストを取得 | 表示が `125:07` になる（hh は使わず分が加算される） |
| **TC-010** | Unit | DD-003 | REQ-006 | 9分5秒経過 | 表示テキストを取得 | 表示が `09:05` になる（2桁ゼロ埋め） |
| **TC-011** | Unit | DD-003 | REQ-007 | 999分59秒に到達 | toggle() を呼ぶ | Limit 状態になり `999:59` のまま変化しない |
| **TC-012** | Unit | DD-003, DD-001 | REQ-008 | 1.999秒経過 | 表示テキストを取得 | `00:01`（秒未満切り捨て） |
| **TC-013** | Unit | DD-003, DD-001 | REQ-008 | 計測中にスリープ（FakeTimeSource で 10 秒進める） | 状態を取得 | スリープ経過分も含めた時間になる |
| **TC-014** | Unit | DD-003, DD-006 | REQ-009 | 計測中または停止中 | reset() または非表示化 | Idle (`00:00`) に初期化される |
| **TC-015** | Unit | DD-006 | REQ-010 | オーバーレイ表示 | WindowManager.LayoutParams を検証 | `FLAG_KEEP_SCREEN_ON` が設定されている |
| **TC-016** | Compose UI | DD-005, SCR-002 | REQ-011 | FloatingStopwatch 表示中 | ドラッグジェスチャを行う | onDrag コールバックが呼ばれ移動する |
| **TC-017** | Unit | DD-004 | REQ-012 | 画面サイズ 1080x2400 で初回ロード | 初期位置を取得 | 右端・中央付近に配置される |
| **TC-018** | Unit | DD-004 | REQ-012 | ドラッグ後の位置 (100, 200) を保存 | 再読み込み | 保存された (100, 200) が復元される |
| **TC-019** | Compose UI | DD-005, SCR-002 | REQ-013 | 各状態 (Idle, Running, Paused) | 色と stateDescription を確認 | それぞれ固有の色とアクセシビリティ状態が割り当てられている |
| **TC-020** | Compose UI | DD-005, SCR-002 | REQ-014 | Idle でタップ | HapticFeedback を検証 | タップ時にハプティクスが要求される |
| **TC-021** | Unit | DD-003, DD-001 | REQ-015 | 計測中にロック（FakeTimeSource で 30 秒進める） | 時間を取得 | 30 秒加算された経過時間が得られる |
| **TC-022** | Unit | DD-006, DD-007 | REQ-016 | プロセス終了 / 非表示 | クリーンアップ処理実行 | リセットされ次回は `00:00` から開始 |
| **TC-023** | Compose UI | DD-009, SCR-001 | REQ-017 | UsageScreen を描画 | 各ステップテキストの存在確認 | 使い方 4 ステップと注記が表示される |
| **TC-024** | Unit | DD-007 | REQ-018 | クイック設定タイルメタデータ | マニフェストとリソースの確認 | タイル名とタイマーアイコンが設定されている |
| **TC-025** | Unit | AndroidManifest | NFR-001 | マージ済みマニフェスト | 宣言パーミッションを検証 | `SYSTEM_ALERT_WINDOW` 以外の不要パーミッション（FGS, WAKE_LOCK 等）が存在しない |
| **TC-026** | Unit | build.gradle.kts | NFR-002 | プロジェクト設定 | minSdk / targetSdk を検証 | minSdk 31, targetSdk 36 |
| **TC-027** | Compose UI | DD-005, SCR-002 | NFR-003 | TalkBack セマンティクス | semantics ノードを検証 | contentDescription, stateDescription, onClick ラベルが存在 |
| **TC-028** | Unit | DD-003, DD-001 | NFR-004 | 実時間と表示テキスト | 100秒間の連続更新テスト | 差分が常に 1 秒未満で正確に追従する |
| **TC-029** | Unit | DD-006 | NFR-005 | show() 呼び出し | 処理完了時間を計測 | 即座に完了（< 500ms） |
| **TC-030** | Unit | DD-003 | NFR-006 | 1 秒未満の経過 | Flow の emit 回数を検証 | 毎秒 1 回のみ emit される（毎フレーム再描画しない） |
| **TC-031** | Unit | res/values | NFR-007 | 英語・日本語リソース | strings.xml を比較検証 | 全リソースキーが日英双方に存在する |
| **TC-032** | Unit | AndroidManifest | NFR-008 | パーミッション・ネットワーク設定 | マニフェスト検査 | `INTERNET` 権限が存在しない |

## 3. E2E ジャーニー
| ID | ファイル | シナリオ | 検証する要件 |
|---|---|---|---|
| **JR-001** | `journeys/JR-001-launch-and-usage.xml` | アプリを起動し、使い方説明画面が正常に表示されることを確認する | REQ-017 |
| **JR-002** | `journeys/JR-002-tile-and-stopwatch.xml` | タイルを操作してストップウォッチを表示し、タップで開始・一時停止できることを確認する | REQ-001, REQ-002, REQ-005 |

## 4. トレーサビリティ（要件 → TC）
| 要件 | TC | JR |
|---|---|---|
| REQ-001 | TC-001 | JR-002 |
| REQ-002 | TC-002 | JR-002 |
| REQ-003 | TC-003 | - |
| REQ-004 | TC-004 | - |
| REQ-005 | TC-005, TC-006, TC-007 | JR-002 |
| REQ-006 | TC-008, TC-009, TC-010 | - |
| REQ-007 | TC-011 | - |
| REQ-008 | TC-012, TC-013 | - |
| REQ-009 | TC-014 | - |
| REQ-010 | TC-015 | - |
| REQ-011 | TC-016 | - |
| REQ-012 | TC-017, TC-018 | - |
| REQ-013 | TC-019 | - |
| REQ-014 | TC-020 | - |
| REQ-015 | TC-021 | - |
| REQ-016 | TC-022 | - |
| REQ-017 | TC-023 | JR-001 |
| REQ-018 | TC-024 | - |
| NFR-001 | TC-025 | - |
| NFR-002 | TC-026 | - |
| NFR-003 | TC-027 | - |
| NFR-004 | TC-028 | - |
| NFR-005 | TC-029 | - |
| NFR-006 | TC-030 | - |
| NFR-007 | TC-031 | - |
| NFR-008 | TC-032 | - |

## 5. テストダブル方針
- **時刻**: `FakeTimeSource(var currentMillis: Long)` を作成し、テストケース内で任意のミリ秒を進めてステートマシンを検証。
- **UI テスト**: `createComposeRule()` (Robolectric) を使い、Composable のタグ・表示テキスト・クリック・アクセシビリティを検証。
- **モック**: 外部フレームワークは使わず、シンプルな Fake クラスで完結させる。

## 6. 未決事項
なし

## 7. 変更履歴
| 日付 | 版 | 内容 |
|---|---|---|
| 2026-10-12 | 1.0 | 初版作成（全要件・非機能要件を網羅する TC-001〜TC-032 および JR-001, JR-002 を策定） |

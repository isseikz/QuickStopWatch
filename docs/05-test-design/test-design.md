<!-- TEMPLATE -->
<!-- このマーカー行は、内容を記入し始めたら削除する -->
# テスト設計書

| 項目 | 内容 |
|---|---|
| バージョン | 0.1 |
| 状態 | DRAFT / IN_REVIEW / APPROVED |
| 入力 | requirements.md v_, ui-design.md v_, detailed-design.md v_ |

## 1. テスト方針（テストピラミッド）
| レベル | ツール | 場所 | 対象 | 実行タイミング |
|---|---|---|---|---|
| Unit | JUnit4, coroutines-test, Turbine | `app/src/test` | Domain / ViewModel / Data | L1 毎回 |
| Compose UI | Robolectric + compose-ui-test | `app/src/test` | 画面の振る舞い | L1 毎回 |
| Screenshot | Roborazzi（@Preview 自動撮影） | `app/src/test/screenshots` | 見た目の回帰 | L2 / verify |
| E2E | Android CLI Journey | `journeys/*.xml` | ユーザーシナリオ | L4 / リリース前 |

## 2. テストケース
<!-- 1 TC = 1 行。TC ID を太字で定義し、検証する REQ/NFR と DD を必ず書く（trace-check が検査） -->
| ID | レベル | 対象 (DD/SCR) | 検証する要件 | 前提 (Given) | 操作 (When) | 期待結果 (Then) |
|---|---|---|---|---|---|---|
| **TC-001** | Unit | DD-001 | REQ-001 |  |  |  |

## 3. E2E ジャーニー
| ID | ファイル | シナリオ | 検証する要件 |
|---|---|---|---|
| **JR-001** | `journeys/JR-001-xxx.xml` |  | REQ-001 |

## 4. トレーサビリティ（要件 → TC）
| 要件 | TC | JR |
|---|---|---|
| REQ-001 | TC-001 | JR-001 |

## 5. テストダブル方針
<!-- Fake を優先。時刻は FakeClock 等で制御 -->

## 6. 未決事項
| ID | 質問 | 回答 | 状態 |
|---|---|---|---|

## 7. 変更履歴
| 日付 | 版 | 内容 |
|---|---|---|

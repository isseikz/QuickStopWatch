# QuickStopWatch — Agent Charter

このファイルはエージェントが常に従うプロジェクト憲章です。詳細は各リンク先を参照してください。

## 0. 作業開始時に必ずやること
1. [`docs/PROGRESS.md`](docs/PROGRESS.md) を読み、**現在のフェーズ・ゲート状態・次アクション**を把握する。
2. 現在フェーズに対応するスキル（`.agents/skills/phase-*/SKILL.md`）を読み、その手順に従う。
3. 作業を終える前に `docs/PROGRESS.md` を更新する（ループの状態はここにしか残らない）。

## 1. プロダクト
- アプリ名: QuickStopWatch / applicationId: `com.puzzroom.quickstopwatch`
- 対象: Android 12 (API 31) 以上 / targetSdk 36
- UI: Jetpack Compose + Material 3、単一 `:app` モジュール（分割はフェーズ3の ADR で決める）

## 2. 開発プロセス（L3 フェーズゲートループ）
| # | フェーズ | スキル | 成果物 |
|---|---|---|---|
| 1 | 要件定義 | `phase-1-requirements` | `docs/01-requirements/requirements.md` |
| 2 | UI 設計 | `phase-2-ui-design` | `docs/02-ui-design/ui-design.md` |
| 3 | アーキテクチャ設計 | `phase-3-architecture` | `docs/03-architecture/architecture.md`, `docs/adr/*` |
| 4 | 詳細設計 | `phase-4-detailed-design` | `docs/04-detailed-design/detailed-design.md` |
| 5 | テスト設計 | `phase-5-test-design` | `docs/05-test-design/test-design.md`, `journeys/*.xml` |
| 6 | 実装 (TDD) | `phase-6-tdd-implementation` | `app/src/**`, テスト |

- **ゲート**: 各フェーズはセルフレビュー（スキル内チェックリスト）→ **ユーザー承認** を経て次へ進む。承認なしでフェーズを進めない。
- 上流成果物に誤りを見つけたら、下流で勝手に吸収せず上流ドキュメントを修正提案する（差し戻し）。
- ループの定義は [`docs/process/loops.md`](docs/process/loops.md)、完了の定義は [`docs/process/definition-of-done.md`](docs/process/definition-of-done.md)。

## 3. コマンド
| 目的 | コマンド |
|---|---|
| 全検証（唯一の正。CI と同一） | `./scripts/verify.sh` |
| 高速検証（TDD 内側ループ） | `./scripts/verify.sh --fast` |
| 特定テスト | `./gradlew :app:testDebugUnitTest --tests "*ClassName*"` |
| 整形 | `./gradlew spotlessApply` |
| スクリーンショット記録 / 比較 | `./gradlew recordRoborazziDebug` / `./gradlew verifyRoborazziDebug` |
| トレーサビリティ検査 | `./scripts/trace-check.sh` |
| 実機/エミュレータ実行 | `android emulator start <avd>` → `android run --apks=app/build/outputs/apk/debug/app-debug.apk` |
| UI 確認 | `android layout --pretty` / `android screen capture -o <file>.png` |
| 公式ドキュメント | `android docs search "<keywords>"` |

- JDK 17 を使う（`JAVA_HOME` 未設定時は `/usr/libexec/java_home -v 17`）。
- Android CLI の使い方は `android-cli` スキルを参照。

## 4. 禁止事項（ハーネスのフックでも強制される）
- 署名鍵（`*.jks`, `*.keystore`）、`keystore.properties`、サービスアカウント JSON を読む・表示する・コミットする。
- `git push --force`、`main` への直接 push（PR 経由。リリースは `v*` タグ push のみ）。
- テストを削除・`@Ignore` してビルドを通す。失敗するテストは原因を直すか、ユーザーに相談する。
- `libs.versions.toml` を経由しない依存追加。
- `verify.sh` が red のまま作業を完了扱いにする。

## 5. コミット規約
- Conventional Commits（`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `build:`, `ci:`, `chore:`）。
- 関連 ID を本文に記載（例: `Refs: REQ-003, TC-012`）。
- 1 コミット = 1 つの green な状態（TDD の Red 状態はコミットしない）。

## 6. リリース
[`RELEASE.md`](RELEASE.md) と `release` スキルを参照。`main` 上のコミットに `vX.Y.Z` タグを打って push すると GitHub Actions が署名ビルド → Play 内部テスト → GitHub Release を行う。

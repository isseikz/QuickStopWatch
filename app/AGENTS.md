# app/ — Coding & TDD Rules

## Kotlin / Compose
- フォーマットは Spotless (ktfmt Google style)。手で整形せず `./gradlew spotlessApply`。
- Composable は **stateless な本体 + stateful なラッパー**に分ける（state hoisting）。本体はテスト・Preview・スクリーンショットの対象。
- Composable の引数順: 必須パラメータ → `modifier: Modifier = Modifier` → 任意パラメータ → コールバック。
- 全ての画面・主要コンポーネントに `@Preview` を用意する（Roborazzi の撮影対象になる）。
- 文字列は `res/values/strings.xml`（日本語は `values-ja`）に置く。ハードコード禁止。
- テストから要素を特定するため `Modifier.testTag(...)` を使う。タグ定数は対象 Composable と同じファイルに `object XxxTestTags` として置く。
- 時刻・乱数・Dispatcher などの非決定要素は直接使わず、インターフェース経由で注入する（テスト容易性）。

## テストの配置
| 種別 | 場所 | 実行 |
|---|---|---|
| ロジック / ViewModel 単体 | `src/test` | JVM |
| Compose UI 振る舞い | `src/test`（Robolectric, `@RunWith(AndroidJUnit4::class)`） | JVM |
| スクリーンショット | `src/test`（Roborazzi） | JVM |
| 実機依存（必要な場合のみ） | `src/androidTest` | エミュレータ |
| E2E | `/journeys/*.xml` | `journey-test` スキル |

## TDD 規約（L1 ループ）
1. `docs/05-test-design/test-design.md` から TC を 1 件選ぶ。
2. **Red**: テストを書き、対象テストだけを実行して**期待した理由で失敗する**ことを確認する。
3. **Green**: テストを通す最小の実装。
4. **Refactor**: 重複除去・命名改善。テストは green のまま。
5. `./scripts/verify.sh --fast` → green ならコミット。
- テスト名には TC ID を含める: `` fun `TC-012 start button starts elapsed time`() ``。
- アサーションは 1 テスト 1 振る舞い。Fake を優先し、Mock フレームワークは必要になるまで導入しない。
- コルーチンは `runTest` + `StandardTestDispatcher`、Flow は Turbine。

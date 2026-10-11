# docs/ — Documentation Rules

## ID 採番
| 接頭辞 | 意味 | 定義場所 |
|---|---|---|
| `REQ-001` | 機能要件 | `01-requirements/requirements.md` |
| `NFR-001` | 非機能要件 | `01-requirements/requirements.md` |
| `SCR-001` | 画面 | `02-ui-design/ui-design.md` |
| `ADR-0001` | アーキテクチャ決定 | `adr/NNNN-*.md` |
| `DD-001` | 詳細設計要素 | `04-detailed-design/detailed-design.md` |
| `TC-001` | テストケース | `05-test-design/test-design.md` |
| `JR-001` | ジャーニー（E2E） | `/journeys/JR-001-*.xml` |

- ID は**定義行**で `**REQ-001**` のように太字で書く（`trace-check.sh` は太字を定義、それ以外を参照とみなす）。
- ID は再利用しない。廃止時は `~~REQ-004~~ (廃止: 理由)` とする。
- 下流ドキュメントの各項目は必ず上流 ID を参照する（例: `DD-003 (REQ-001, SCR-002)`）。

## 書き方
- テンプレートの見出し構造を崩さない。該当なしは「なし」と書く（空欄にしない）。
- 曖昧語（「適切に」「なるべく」「速く」）を避け、測定可能な基準で書く。
- 図は Mermaid を使う。
- 未決事項は各ドキュメント末尾の「未決事項」に `Q-xx` として記録し、ユーザーに確認する。

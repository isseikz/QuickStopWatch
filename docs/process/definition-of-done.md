# Definition of Done

## タスク（TC 単位）
- [ ] TC ID を含むテストが存在し green
- [ ] `./scripts/verify.sh --fast` green
- [ ] Conventional Commit でコミット（`Refs:` に ID）

## 機能（REQ 単位）
- [ ] REQ に紐づく全 TC が green
- [ ] UI 変更がある場合、Roborazzi ベースラインを更新し、設計と一致することを確認
- [ ] 関連 Journey が PASS
- [ ] `./scripts/verify.sh` green

## フェーズ
- [ ] スキルのセルフレビュー チェックリストを全て満たす
- [ ] ユーザー承認済み
- [ ] `docs/PROGRESS.md` 更新済み

## リリース
- [ ] `main` の CI が green
- [ ] 全 Journey PASS（直近の結果が `journey-results/` にある）
- [ ] `app/src/main/play/release-notes/ja-JP/default.txt` を更新
- [ ] `vX.Y.Z` タグが前回より大きい

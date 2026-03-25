---
name: check-pr-conflicts-before-review
enabled: true
event: prompt
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: (code.?review|coderabbit|review.?skill|pr.*review|review.*pr)
---

## PR Conflict Kontrolü Zorunlu

Code review başlatılmadan önce PR'ın conflict durumunu kontrol et ve gerekirse düzelt:

1. **Conflict kontrolü:**
   ```bash
   gh pr view <pr_number> --json mergeable,mergeStateStatus
   ```

2. **Eğer `"mergeable":"CONFLICTING"` ise:**
   ```bash
   git fetch origin main
   git merge origin/main --no-edit
   # Conflict'leri çöz
   git add .
   git commit -m "chore: resolve merge conflicts with main"
   git push
   ```

3. **Conflict yoksa veya çözüldükten sonra** code review skill'ini çalıştır.

Conflict'li bir PR'ı review etmek yanıltıcı sonuçlar verebilir.

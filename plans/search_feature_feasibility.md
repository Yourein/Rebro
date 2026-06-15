# 検索機能の技術的実現可能性調査

書名 + 管理番号（ISBN / ISDN）での曖昧検索が可能かどうかの調査メモ。
テキストボックス1つに入力された値で、タイトル・管理番号を区別せず自動的に検索する想定。

## 結論

現在のスキーマのまま、`BookDao` に検索クエリを1つ追加するだけで実現可能。
入力がタイトルか管理番号かをアプリ側で判別する必要はなく、3カラムへの OR 検索で自然に満たせる。

## データ構造の確認

書名と管理番号は別テーブルに分かれている。

- `books` … `title`（書名）を保持。`book_type` で COMMERCIAL / DOUJIN を区別。
- `commercial_book_details` … `isbn`（商業本の管理番号）を保持。`book_id` で `books` と 1:1。
- `doujin_book_details` … `isdn`（同人本の管理番号）を保持。`book_id` で `books` と 1:1。

1冊の本は book_type に応じて ISBN か ISDN の **どちらか一方** を持つ構造。

関連定義:
- `BookWithDetail`（model/relation）が `Book` + `CommercialBookDetail?` + `DoujinBookDetail?` を束ねている。

## 実装イメージ

`BookDao` に以下を追加するだけでよい。

```kotlin
@Transaction
@Query(
    """
    SELECT DISTINCT b.* FROM books b
    LEFT JOIN commercial_book_details c ON b.id = c.book_id
    LEFT JOIN doujin_book_details d ON b.id = d.book_id
    WHERE b.title LIKE '%' || :query || '%'
       OR c.isbn  LIKE '%' || :query || '%'
       OR d.isdn  LIKE '%' || :query || '%'
    ORDER BY b.id ASC
    """
)
fun searchBooks(query: String): Flow<List<BookWithDetail>>
```

- `LEFT JOIN` にしているのは、商業本には doujin_detail が、同人本には commercial_detail が存在しないため。全件を取りこぼさず検索できる。
- 併せて `interfaces/BookRepository` と `BookRepositoryImpl` への委譲メソッド追加、検索画面の追加が必要。

## 考慮点

### 1. ISBN / ISDN のハイフン揺れ
`978-4-...` のようにハイフン付きで保存されていると、ハイフン無し入力でヒットしない。
対策: 保存時に正規化（ハイフン除去）して持つ、または検索時に両側からハイフン除去して比較（`REPLACE(c.isbn, '-', '')` 等）。曖昧検索を快適にするなら検討推奨。

### 2. 大文字小文字 / 全角半角
SQLite の `LIKE` は ASCII についてはデフォルトで大文字小文字を区別しない（日本語書名には影響なし）。
全角数字で打たれた管理番号の揺れを吸収したい場合は別途正規化が必要。

### 3. パフォーマンス
`LIKE '%...%'`（前方一致でない）はインデックスが効かず全走査になる。

冊数の目安（Android 上の SQLite、オーダー感）:

| 冊数 | 体感 |
|------|------|
| 〜1万冊 | ほぼ無感（数ms〜十数ms）|
| 1万〜5万冊 | 単発検索なら問題なし（十数〜数十ms）|
| 10万冊〜 | 数十〜数百msに達しうる。ここで初めて「遅い」と感じる可能性 |

懸念が現実化し始めるのはおよそ **10万冊オーダー**。

ただし冊数の絶対値より **検索クエリの呼び方** の方が体感に効く。
- インクリメンタルサーチ（1打鍵ごとに検索）にすると "打鍵回数 × クエリコスト" で積み重なる。
- 対策は **入力のデバウンス（入力停止後 200〜300ms で検索）**。冊数に関係なく効く。

#### このアプリでの方針
- 個人蔵書管理という用途では現実的に多くても数千冊規模 → `LIKE` 全走査で全く問題なし。
- 当面は素直な `LIKE` 実装でOK。早期の FTS 導入は不要。
- 検索画面を作るなら **最初からデバウンスは入れておく**。
- FTS4/FTS5 への移行は「数万冊超」かつ「インクリメンタルサーチで体感低下」を実測してからで十分間に合う。

## 参照ファイル
- `model/src/main/java/net/yourein/rebro/model/entity/Book.kt`
- `model/src/main/java/net/yourein/rebro/model/entity/CommercialBookDetail.kt`
- `model/src/main/java/net/yourein/rebro/model/entity/DoujinBookDetail.kt`
- `model/src/main/java/net/yourein/rebro/model/relation/BookWithDetail.kt`
- `repositories/src/main/java/net/yourein/rebro/repositories/BookDao.kt`
- `interfaces/src/main/java/net/yourein/rebro/interfaces/BookRepository.kt`

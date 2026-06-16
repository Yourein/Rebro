# データベース設計（Room / SQLite）

## 要件整理

- 商業本・同人本の両方を管理する
- 本は本棚に所属する
- 読了状況（3ステータス）を記録できる
- 本にはサムネイル画像があり、アプリのストレージに保存される
- 実装は Android Room（SQLite）を使用する

---

## ER図（概念）

```
bookshelves
    │
    └─< books (bookshelf_id)
            │
            ├─ commercial_book_details (book_id)  ← 1対1
            ├─ doujin_book_details     (book_id)  ← 1対1
            └─< book_authors (book_id) >─ authors
```

---

## テーブル定義

### `bookshelves`（本棚）

| カラム名 | 型      | 制約              | 説明   |
|----------|---------|-------------------|--------|
| id       | INTEGER | PK, AUTOINCREMENT | 本棚ID |
| name     | TEXT    | NOT NULL          | 本棚名 |

---

### `books`（本・共通）

| カラム名       | 型      | 制約                       | 説明                                               |
|----------------|---------|----------------------------|----------------------------------------------------|
| id             | INTEGER | PK, AUTOINCREMENT          | 本ID                                               |
| bookshelf_id   | INTEGER | FK → bookshelves.id        | 所属する本棚                                       |
| title          | TEXT    | NOT NULL                   | タイトル                                           |
| subtitle       | TEXT    |                            | サブタイトル                                       |
| thumbnail_path | TEXT    |                            | サムネイル画像のローカルパス（アプリ内ストレージ） |
| book_type      | TEXT    | NOT NULL                   | 種別: `COMMERCIAL` / `DOUJIN`                      |
| reading_status | TEXT    | NOT NULL, DEFAULT `UNREAD` | 読了状況（後述）                                   |
| memo           | TEXT    |                            | メモ                                               |

#### `reading_status` の取りうる値

| 値          | 意味           |
|-------------|----------------|
| `UNREAD`    | 読んでいない   |
| `READING`   | 読んでいる途中 |
| `COMPLETED` | 読んだ         |

---

### `commercial_book_details`（商業本の追加情報）

`books.book_type = 'COMMERCIAL'` の本と1対1で紐づく。

| カラム名  | 型      | 制約                  | 説明     |
|-----------|---------|-----------------------|----------|
| id        | INTEGER | PK, AUTOINCREMENT     |          |
| book_id   | INTEGER | FK → books.id, UNIQUE | 対象の本 |
| isbn      | TEXT    |                       | ISBN     |
| publisher | TEXT    |                       | 出版社   |

---

### `doujin_book_details`（同人本の追加情報）

`books.book_type = 'DOUJIN'` の本と1対1で紐づく。

| カラム名    | 型      | 制約                  | 説明       |
|-------------|---------|-----------------------|------------|
| id          | INTEGER | PK, AUTOINCREMENT     |            |
| book_id     | INTEGER | FK → books.id, UNIQUE | 対象の本   |
| circle_name | TEXT    |                       | サークル名 |
| isdn        | TEXT    |                       | ISDN       |

---

### `authors`（著者）

| カラム名 | 型      | 制約              | 説明    |
|----------|---------|-------------------|---------|
| id       | INTEGER | PK, AUTOINCREMENT | 著者ID  |
| name     | TEXT    | NOT NULL, UNIQUE  | 著者名  |

---

### `book_authors`（本と著者の結合）

1冊の本に複数著者を紐づける多対多結合テーブル。外部キー制約により参照整合性を保証する。

| カラム名  | 型      | 制約                        | 説明   |
|-----------|---------|-----------------------------|--------|
| book_id   | INTEGER | FK → books.id               | 本ID   |
| author_id | INTEGER | FK → authors.id             | 著者ID |
|           |         | PK (book_id, author_id)     | 複合PK |

---

## Room 実装上のポイント

### 著者付き本の取得（@Relation）

```kotlin
data class BookWithAuthors(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookAuthor::class,
            parentColumn = "book_id",
            entityColumn = "author_id"
        )
    )
    val authors: List<Author>
)
```

### 本棚とそれに属する本の取得（@Relation）

```kotlin
data class BookshelfWithBooks(
    @Embedded val bookshelf: Bookshelf,
    @Relation(parentColumn = "id", entityColumn = "bookshelf_id")
    val books: List<Book>
)
```

### 商業本・同人本の詳細取得（@Relation）

```kotlin
data class BookWithDetail(
    @Embedded val book: Book,
    @Relation(parentColumn = "id", entityColumn = "book_id")
    val commercialDetail: CommercialBookDetail?,
    @Relation(parentColumn = "id", entityColumn = "book_id")
    val doujinDetail: DoujinBookDetail?
)
```

---

## 設計上の判断

### 著者の持ち方
`authors` テーブルを独立させ、`book_authors` 結合テーブルで多対多を表現する。TypeConverterによるリスト保存と異なり外部キー制約が機能するため参照整合性が保たれ、著者名の一括修正や著者を軸にした本の絞り込み検索も可能になる。

### 商業本・同人本の分離方法
`books` テーブルに `book_type` カラムを置き、種別固有情報は別テーブルに分離するクラステーブル継承を採用。`books` テーブルを共通クエリの起点にしつつ、nullable カラムの混在を避ける。

### サムネイル画像の管理
画像ファイルはアプリの内部ストレージ（`filesDir` 配下）に保存し、`books.thumbnail_path` にパス文字列のみ格納。画像バイナリはDBに持たない。

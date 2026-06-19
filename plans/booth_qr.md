# Import from Rebro QR

方針としてはAutofill by ISDN/ISBN BarCodeと同様で、以下のクラスで埋める。

```kotlin
data class AutofillResult(
    val title: String,
    val bookType: BookType,
    val publisher: String,
    val authorNames: List<String>,
    val circleName: String?,
    val coverImageUrl: String?,
)
```

現在は `AutofillResult` は `@Serializable` ではないが、 `@Serializable` に変更し、`String (JSON String) → AutofillResult` ができるようにする。  
もちろん、`bookType` も `Serializable` に変更する。

## Booth Item Page → Rebro QR

`*.booth.pm/items/*` にマッチするページに存在する時、`js-item-share-buttons` の左側に「Rebro QRを表示」ボタンを作成する。  
ボタンを押すと何らかの形でRebro QR (AutofillResultをStringにシリアライズし、バイナリ形式で詰めたもの) を表示する。

ページ内容とAutofillResultのメンバは以下のようにマッピングする。

- (HTMLの)titleを `$1 - $2 - BOOTH` という形式で捉えた時、
  - `$1` → `title`
  - `$2` → `circleName`
- `bookType` → 場合によらず `DOUJIN`
- `authorNames` → ページをHTMLで見た時、 `class=home-link-container__nickname` のDiv配下に存在する a タグで記述されたユーザー名
- `coverImageUrl` → `market-item-detail-item-image-wrapper` 配下に存在する `img` タグのうち、親に `data-stick-index=0` の属性を持つものの `src`


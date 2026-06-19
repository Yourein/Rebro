# ISDN API 連携の実装

## 概要

ISDN（国際標準同人誌番号）の Web API から書籍情報を取得し、パースする機能を実装した。

- API エンドポイント: `https://isdn.jp/xml/{ISDN番号}`
- レスポンス形式: XML（名前空間 `https://isdn.jp/schemas/0.1`）

## 構成

### 依存ライブラリ

| ライブラリ | 用途 |
|---|---|
| Retrofit2 | HTTP クライアント |
| OkHttp + logging-interceptor | HTTP 通信・リクエスト/レスポンスのログ出力 |
| xmlutil-serialization | kotlinx.serialization ベースの XML パーサー |

### モジュール構成

- **model** (`net.yourein.rebro.model.isdn`)
  - `IsdnResponse` / `IsdnItem` / `IsdnUserOption` / `IsdnExternalLink` — XML レスポンスに対応するデータクラス群
- **interfaces** (`net.yourein.rebro.interfaces`)
  - `IsdnRepository` — ISDN API リポジトリのインターフェース
- **repositories** (`net.yourein.rebro.repositories`)
  - `IsdnApiService` — Retrofit サービスインターフェース
  - `IsdnRepositoryImpl` — API 呼び出し + XML デシリアライズ
- **core/application**
  - `BaseApplication` — Koin の `networkKoinModule` で Retrofit / OkHttpClient / XML / IsdnRepository を DI 登録
- **feature/register-top**
  - `IsdnDebugScreen` / `IsdnDebugViewModel` — ISDN 番号を入力して API レスポンスを確認するデバッグ画面
- **core/navigation**
  - `IsdnDebug` NavKey — デバッグ画面へのナビゲーション先

### 画面遷移

Register Top 画面 → 「Autofill by ISDN/ISBN barcode」タップ → IsdnDebug 画面

## xmlutil パース時のハマりどころ

xmlutil-serialization で ISDN の XML をパースする際、以下の3点で問題が発生した。

### 1. `@XmlChildrenName` によるリストのラップ問題

```kotlin
// NG: ラップされたリスト構造を期待してしまう
@XmlChildrenName("item", NS, "")
val item: List<IsdnItem>? = null,

// OK: インラインリストとして子型の @XmlSerialName で自動マッチ
val item: List<IsdnItem>? = null,
```

`@XmlChildrenName` を付けると、xmlutil はプロパティ名のラッパー要素の中に子要素があると期待する。
ISDN の XML では `<item>` が `<isdn>` 直下にインラインで並ぶため、アノテーションなしが正しい。
`IsdnItem` 側の `@XmlSerialName("item", NS, "")` だけで要素名マッチングが行われる。

### 2. `@XmlElement(true)` が必要

```kotlin
// NG: xmlutil はデフォルトで String を XML 属性として扱う
@XmlSerialName("product-name", NS, "")
val productName: String? = null,

// OK: 子要素であることを明示
@XmlElement(true) @XmlSerialName("product-name", NS, "")
val productName: String? = null,
```

xmlutil のデフォルト動作では、プリミティブ型（String, Int 等）は XML 属性として扱われる。
ISDN の XML では各フィールドが子要素として返るため、`@XmlElement(true)` で明示する必要がある。
`key` 属性（`<item key="...">`）のみ属性のままにする。

### 3. テキストノード（改行・空白）のハンドリング

XML のタグ間にある改行や空白がテキストノードとして解釈され、
マッチするフィールドが見つからず `UnknownXmlFieldException` が発生する。

```kotlin
// XML 設定で未知のコンテンツを無視する
XML {
    recommended()
    policy = DefaultXmlSerializationPolicy.Builder().apply {
        pedantic = false
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }.build()
}
```

`xsi:schemaLocation` などの名前空間属性も同様に未知コンテンツとして扱われるため、
`IGNORING_UNKNOWN_CHILD_HANDLER` で一括して無視する。

## テスト

`model/src/test/java/.../isdn/IsdnResponseParseTest.kt` にて、
実際の API レスポンス XML を使ったパーステストを実装済み。

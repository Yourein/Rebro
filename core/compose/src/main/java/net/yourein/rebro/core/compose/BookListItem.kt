package net.yourein.rebro.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.core.resources.RebroTheme

/**
 * 書影・タイトル・著者(商業出版)/サークル名(同人出版)を 1 行で表示する、
 * 再利用を前提とした書籍リストアイテム。
 *
 * タイトルや著者情報、クリック時の挙動はすべて親 Composable から受け取る。
 *
 * @param title 書籍タイトル。Bold・最大 2 行で表示し、収まらない場合は末尾を省略する。
 * @param author 著者名(商業出版)またはサークル名(同人出版)。最大 1 行で末尾を省略する。
 * @param coverImageUrl 書影画像の URL。null の場合はプレースホルダ領域のみ表示する。
 * @param onClick アイテム全体がクリックされた際に呼び出されるコールバック。
 */
@Composable
fun BookListItem(
    title: String,
    author: String,
    coverImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = coverImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(72.dp)
                .aspectRatio(COVER_ASPECT_RATIO)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = RebroColor.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = author,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = RebroColor.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 書影の縦横比(横 : 縦 = 2 : 3 を想定した一般的な書籍カバー比率)。 */
private const val COVER_ASPECT_RATIO = 2f / 3f

@Preview(showBackground = true)
@Composable
private fun BookListItemPreview() {
    RebroTheme {
        BookListItem(
            title = "タイトル".repeat(10),
            author = "作家/サークル".repeat(10),
            coverImageUrl = null,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

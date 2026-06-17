package net.yourein.rebro.feature.searchtop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.core.resources.RebroTheme

@Composable
fun SearchTopScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader {
            SearchTopBar(
                onClick = {},
                modifier = Modifier
                    .background(RebroColor.Background)
                    .padding(all = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun SearchTopBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(DrawableR.search_36dp_fill),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Type Title or ISDN(ISBN) to search",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchTopBarPreview() {
    RebroTheme {
        SearchTopBar(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
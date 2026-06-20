package net.yourein.rebro.feature.bookshelfs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.model.entity.Bookshelf
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookshelvesScreen(
    navigateBack: () -> Unit,
    viewModel: BookshelvesViewModel = koinViewModel(),
) {
    val bookshelvesState by viewModel.bookshelvesState.collectAsStateWithLifecycle()
    BookshelvesScreen(
        bookshelvesState = bookshelvesState,
        navigateBack = navigateBack,
    )
}

@Composable
fun BookshelvesScreen(
    bookshelvesState: LoadingState<List<Bookshelf>>,
    navigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    )
                    .height(56.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(DrawableR.chevron_forward_24dp_fill),
                    contentDescription = "Back",
                    tint = RebroColor.TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable { navigateBack() }
                        .padding(8.dp)
                        .size(24.dp)
                        .scale(scaleX = -1f, scaleY = 1f)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Bookshelves",
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RebroColor.TextPrimary,
                )
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        when (bookshelvesState) {
            is LoadingState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadingState.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(text = "Error!")
                }
            }

            is LoadingState.Success -> {
                val bookshelves = bookshelvesState.value
                if (bookshelves.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Text(text = "No Bookshelves.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(bookshelves) { bookshelf ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = bookshelf.name,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    color = RebroColor.TextPrimary,
                                )
                            }
                            if (bookshelf != bookshelves.last()) {
                                HorizontalDivider(
                                    color = Color.Gray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

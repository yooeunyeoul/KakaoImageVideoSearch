package com.example.kakaoimagevideosearch.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.flow.collectLatest

//@Composable
//fun SearchScreen(
//    viewModel: SearchViewModel = mavericksViewModel(),
//    onNavigateToBookmark: () -> Unit
//) {
//    val state by viewModel.collectAsState()
//    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
//    var searchQuery by remember { mutableStateOf("") }
//
//    LaunchedEffect(Unit) {
//        viewModel.effect.collectLatest { effect ->
//            when (effect) {
//                is SearchEffect.ShowError -> {
//                    // TODO: Show error message using Snackbar or Dialog
//                }
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                modifier = Modifier.weight(1f),
//                placeholder = { Text("검색어를 입력하세요") },
//                singleLine = true
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Button(onClick = { viewModel.onEvent(SearchEvent.Search(searchQuery)) }) {
//                Text("검색")
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Button(onClick = onNavigateToBookmark) {
//                Text("보관함")
//            }
//        }
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(searchResults) { item ->
//                if (item != null) {
//                    SearchResultItem(item = item)
//                }
//            }
//        }
//    }
//}

//@Composable
//fun SearchResultItem(
//    item: SearchResult,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            AsyncImage(
//                model = item.thumbnailUrl,
//                contentDescription = item.title,
//                modifier = Modifier.size(80.dp)
//            )
//
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text(
//                    text = item.title,
//                    style = MaterialTheme.typography.titleMedium
//                )
//                Text(
//                    text = item.datetime,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                Text(
//                    text = item.source,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            IconButton(onClick = { /* TODO: Implement bookmark functionality */ }) {
//                Icon(
//                    imageVector = if (false /* TODO: Check if bookmarked */) {
//                        androidx.compose.material.icons.Icons.Default.Favorite
//                    } else {
//                        androidx.compose.material.icons.Icons.Default.FavoriteBorder
//                    },
//                    contentDescription = "북마크"
//                )
//            }
//        }
//    }
//}
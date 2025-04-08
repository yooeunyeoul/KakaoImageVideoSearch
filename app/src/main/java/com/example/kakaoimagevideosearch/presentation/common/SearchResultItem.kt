package com.example.kakaoimagevideosearch.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType

/**
 * 검색 결과 아이템을 표시하는 공통 컴포넌트
 * 
 * @param item 표시할 검색 결과 아이템
 * @param isFavorite 좋아요 상태
 * @param onFavoriteClick 좋아요 버튼 클릭 이벤트 핸들러
 * @param showFavoriteButton 좋아요 버튼 표시 여부 (북마크 화면에서는 false)
 */
@Composable
fun SearchResultItem(
    item: SearchResult,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    showFavoriteButton: Boolean = true
) {
    // fade in 애니메이션을 위한 상태
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "alpha"
    )
    
    // 컴포넌트가 처음 렌더링될 때 애니메이션 시작
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .graphicsLayer { 
                this.alpha = alpha 
            }
    ) {
        SubcomposeAsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // 날짜 표시 영역 추가
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp) // 패딩 약간 더 증가
        ) {
            // mapper에서 이미 개행 처리된 날짜/시간 텍스트를 그대로 표시
            Text(
                text = item.datetime,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp, // 줄 간격 지정
                maxLines = 2, // 최대 2줄 표시
                overflow = TextOverflow.Visible // 텍스트가 잘리지 않도록 함
            )
        }

        // 타입 표시 (이미지/비디오)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (item.type == SearchResultType.IMAGE) "IMG" else "VID",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // 하트 아이콘 (북마크 화면에서는 표시하지 않음)
        if (showFavoriteButton) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp) // 크기 축소
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(14.dp)) // 반지름 절반으로
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(14.dp) // 아이콘 크기도 축소
                )
            }
        }
    }
} 
package com.example.kakaoimagevideosearch.data.mapper

import com.example.kakaoimagevideosearch.data.model.ImageDocument
import com.example.kakaoimagevideosearch.data.model.VideoDocument
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

fun ImageDocument.toDomain() = SearchResult(
    id = UUID.randomUUID().toString(),
    thumbnailUrl = thumbnailUrl,
    title = displaySitename,
    source = docUrl,
    datetime = formatDateTime(datetime),
    type = SearchResultType.IMAGE
)

fun VideoDocument.toDomain() = SearchResult(
    id = UUID.randomUUID().toString(),
    thumbnailUrl = thumbnail,
    title = title,
    source = url,
    datetime = formatDateTime(datetime),
    type = SearchResultType.VIDEO
)

fun List<ImageDocument>.toImageDomain() = map { it.toDomain() }
fun List<VideoDocument>.toVideoDomain() = map { it.toDomain() }

/**
 * 날짜 시간 문자열을 포맷팅합니다.
 * 입력 형식: "2023-04-06T12:34:56.000+09:00"
 * 출력 형식: "yyyy.MM.dd\nHH:mm" (날짜와 시간 사이에 개행 추가)
 */
private fun formatDateTime(dateTimeStr: String): String {
    return try {
        // ISO 8601 형식의 날짜 시간 문자열을 파싱
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val date = inputFormat.parse(dateTimeStr) ?: Date()
        
        // 날짜 부분 포맷팅
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val dateStr = dateFormat.format(date)
        
        // 시간 부분 포맷팅 (초 제외)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = timeFormat.format(date)
        
        // 날짜와 시간 사이에 개행 추가
        "$dateStr\n$timeStr"
    } catch (e: Exception) {
        // 파싱 실패 시 원본 반환
        dateTimeStr
    }
} 
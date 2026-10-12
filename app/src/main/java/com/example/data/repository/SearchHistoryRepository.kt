package com.example.data.repository

import com.example.data.db.SearchHistoryDao
import com.example.data.db.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(private val dao: SearchHistoryDao) {

    val allHistory: Flow<List<SearchHistoryItem>> = dao.getAllHistory()

    suspend fun saveQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        // Remove existing occurrence so it bubbles to the top
        dao.deleteByQuery(trimmed)
        dao.insertQuery(SearchHistoryItem(query = trimmed, timestamp = System.currentTimeMillis()))
    }

    suspend fun removeQuery(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}

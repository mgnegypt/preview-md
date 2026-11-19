package com.mgnegypt.previewmd.data.recent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {

    @Query("SELECT * FROM recent_files ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun observeRecents(limit: Int): Flow<List<RecentFile>>

    @Query(
        "SELECT * FROM recent_files WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY lastOpenedAt DESC LIMIT :limit"
    )
    fun searchRecents(query: String, limit: Int): Flow<List<RecentFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(file: RecentFile)

    @Query("DELETE FROM recent_files WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("SELECT uri FROM recent_files ORDER BY lastOpenedAt DESC")
    suspend fun allUrisByRecency(): List<String>

    @Query("DELETE FROM recent_files WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)
}

@Dao
interface FolderRootDao {

    @Query("SELECT * FROM folder_roots ORDER BY addedAt ASC")
    fun observeRoots(): Flow<List<FolderRoot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(root: FolderRoot)

    @Query("DELETE FROM folder_roots WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)
}

package com.mgnegypt.previewmd.data.recent

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecentFile::class, FolderRoot::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao
    abstract fun folderRootDao(): FolderRootDao
}

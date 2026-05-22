package com.jp.foodyvilla_backoffice.data.repo

import com.jp.foodyvilla_backoffice.data.model.backoffice.TaskCategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskCategoryRepository(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest["task_categories"]

    suspend fun getCategories(): List<TaskCategory> = withContext(Dispatchers.IO) {
        postgrest.select().decodeList<TaskCategory>()
    }

    suspend fun insertCategory(category: TaskCategory) = withContext(Dispatchers.IO) {
        postgrest.insert(category)
    }

    suspend fun updateCategory(id: Long, category: TaskCategory) = withContext(Dispatchers.IO) {
        postgrest.update(category) {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        postgrest.delete {
            filter {
                eq("id", id)
            }
        }
    }
}

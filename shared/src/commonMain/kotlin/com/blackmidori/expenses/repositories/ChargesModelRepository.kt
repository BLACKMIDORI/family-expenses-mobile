package com.blackmidori.expenses.repositories

import com.blackmidori.expenses.core.EntityList
import com.blackmidori.expenses.core.PagedList
import com.blackmidori.expenses.models.ChargesModel

class ChargesModelRepository(
    private val store: Store<out EntityList<ChargesModel>>
) {
    suspend fun add(workspaceId: String, entity: ChargesModel): Result<ChargesModel> {
        return store.add { id, creationDateTime ->
            ChargesModel(id, creationDateTime, workspaceId, entity.name)
        }
    }

    suspend fun getPagedList(workspaceId: String): Result<PagedList<ChargesModel>> {
        val result = store.getList();
        return result.map { it ->
            PagedList(
                999,
                0,
                it.filter { it.workspaceId == workspaceId }.toTypedArray()
            )
        }
    }

    suspend fun getOne(id: String): Result<ChargesModel> {
        return store.getOne(id)
    }

    suspend fun update(entity: ChargesModel): Result<ChargesModel> {
        val old = getOne(entity.id).getOrNull()
        return store.update(
            old?.let {
                ChargesModel(it.id, it.creationDateTime, it.workspaceId, entity.name)
            } ?: entity
        )
    }

    suspend fun delete(id: String): Result<Boolean> {
        return store.delete(id)
    }

}
package com.blackmidori.familyexpenses.repositories

import com.blackmidori.familyexpenses.Config
import com.blackmidori.familyexpenses.Session
import com.blackmidori.familyexpenses.core.http.HttpClient
import com.blackmidori.familyexpenses.models.Workspace
import com.blackmidori.familyexpenses.core.PagedList
import com.blackmidori.familyexpenses.services.TokensService
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WorkspaceRepository(
    val baseUrl: String = Config.apiBaseUrl,
    val httpClient: HttpClient,
    val tokensService: TokensService = TokensService(authRepository = AuthRepository(httpClient = httpClient)),
) {
    fun add(workspace: Workspace): Result<Workspace> {
        val result = tokensService.getUpdatedAccessToken()
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val accessToken = result.getOrThrow()

        val requestBody = "{\"name\":\"${workspace.name}\"}"
        val response = httpClient.post(
            "$baseUrl/v1/workspaces/",
            requestBody,
            mapOf("Authorization" to accessToken),
        )
        if (response.status != 200) {
            return Result.failure(Exception(response.body))
        }
        val responseBody = response.body?.let { Json.decodeFromString<JsonObject>(it) }

        if (responseBody == null) {
            return Result.failure(Exception("http fetch error"))
        } else {
            return Result.success(
                Workspace(
                    id = responseBody["id"]!!.jsonPrimitive.content,
                    creationDateTime = Instant.parse(responseBody["creationDateTime"]!!.jsonPrimitive.content),
                    name = responseBody["name"]!!.jsonPrimitive.content
                )
            )
        }
    }

    fun getPagedList(): Result<PagedList<Workspace>> {
        val result = tokensService.getUpdatedAccessToken()
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val accessToken = result.getOrThrow()

        val response = httpClient.get(
            "$baseUrl/v1/workspaces/?size=999&from=0",
            mapOf("Authorization" to accessToken),
        )
        if (response.status != 200) {
            return Result.failure(Exception(response.body))
        }
        val responseBody = response.body?.let { Json.decodeFromString<JsonObject>(it) }

        if (responseBody == null) {
            return Result.failure(Exception("http fetch error"))
        } else {
            val list = ArrayList<Workspace>()
            for (jsonElement in responseBody["results"]!!.jsonArray) {
                val obj = jsonElement.jsonObject
                list.add(
                    Workspace(
                        id = obj["id"]!!.jsonPrimitive.content,
                        creationDateTime = Instant.parse(obj["creationDateTime"]!!.jsonPrimitive.content),
                        name = obj["name"]!!.jsonPrimitive.content
                    )
                )
            }
            return Result.success(
                PagedList(
                    responseBody["size"]!!.jsonPrimitive.int,
                    responseBody["from"]!!.jsonPrimitive.int,
                    list.toTypedArray()
                )
            )
        }
    }

    fun getOne(workspaceId: String): Result<Workspace> {
        val result = tokensService.getUpdatedAccessToken()
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val accessToken = result.getOrThrow()

        val response = httpClient.get(
            "$baseUrl/v1/workspaces/${workspaceId}",
            mapOf("Authorization" to accessToken),
        )
        if (response.status != 200) {
            return Result.failure(Exception(response.body))
        }
        val responseBody = response.body?.let { Json.decodeFromString<JsonObject>(it) }

        if (responseBody == null) {
            return Result.failure(Exception("http fetch error"))
        } else {
            return Result.success(
                Workspace(
                    id = responseBody["id"]!!.jsonPrimitive.content,
                    creationDateTime = Instant.parse(responseBody["creationDateTime"]!!.jsonPrimitive.content),
                    name = responseBody["name"]!!.jsonPrimitive.content
                )
            )
        }
    }

    fun update(workspace: Workspace): Result<Workspace> {
        val result = tokensService.getUpdatedAccessToken()
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val accessToken = result.getOrThrow()

        val requestBody = "{\"name\":\"${workspace.name}\"}"
        val response = httpClient.put(
            "$baseUrl/v1/workspaces/${workspace.id}",
            requestBody,
            mapOf("Authorization" to accessToken),
        )
        if (response.status != 200) {
            return Result.failure(Exception(response.body))
        }
        val responseBody = response.body?.let { Json.decodeFromString<JsonObject>(it) }

        if (responseBody == null) {
            return Result.failure(Exception("http fetch error"))
        } else {
            return Result.success(
                Workspace(
                    id = responseBody["id"]!!.jsonPrimitive.content,
                    creationDateTime = Instant.parse(responseBody["creationDateTime"]!!.jsonPrimitive.content),
                    name = responseBody["name"]!!.jsonPrimitive.content
                )
            )
        }
    }

    fun delete(workspace: Workspace): Result<Boolean> {
        val result = tokensService.getUpdatedAccessToken()
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val accessToken = result.getOrThrow()

        val response = httpClient.delete(
            "$baseUrl/v1/workspaces/${workspace.id}",
            mapOf("Authorization" to accessToken),
        )
        if (response.status != 200) {
            return Result.failure(Exception(response.body))
        }
        val responseBody = response.body?.let { Json.decodeFromString<JsonObject>(it) }

        if (responseBody == null) {
            return Result.failure(Exception("http fetch error"))
        } else {
            return Result.success(
                responseBody["ok"]!!.jsonPrimitive.boolean
            )
        }
    }

}
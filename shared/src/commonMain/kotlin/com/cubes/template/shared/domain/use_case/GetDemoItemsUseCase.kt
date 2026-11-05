package com.cubes.template.shared.domain.use_case

import com.cubes.template.shared.data.remote.RemoteDataSource
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.data.remote.response.DemoItemApi
import com.cubes.template.shared.domain.model.DemoItem

/**
 * Use case = one unit of business logic: calls the data source and maps DTOs to domain models.
 * Shared use cases live here; screen-specific ones live next to the screen in `ui/<screen>/domain/use_case`.
 */
class GetDemoItemsUseCase(private val remoteDataSource: RemoteDataSource) {

    suspend fun getDemoItems(): Result<List<DemoItem>> =
        remoteDataSource.getDemoItems().onSuccess { list -> list.map { it.toDemoItem() } }
}

internal fun DemoItemApi.toDemoItem() = DemoItem(
    id = id,
    title = title.orEmpty().replaceFirstChar { it.uppercase() },
    description = body.orEmpty(),
)

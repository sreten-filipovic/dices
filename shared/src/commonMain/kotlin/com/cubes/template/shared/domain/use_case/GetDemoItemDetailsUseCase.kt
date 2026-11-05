package com.cubes.template.shared.domain.use_case

import com.cubes.template.shared.data.remote.RemoteDataSource
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.model.DemoItem

class GetDemoItemDetailsUseCase(private val remoteDataSource: RemoteDataSource) {

    suspend fun getDemoItem(id: Int): Result<DemoItem> =
        remoteDataSource.getDemoItem(id).onSuccess { it.toDemoItem() }
}

package com.mindmatrix.budakattusante.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mindmatrix.budakattusante.data.local.dao.ProductDao
import com.mindmatrix.budakattusante.data.local.entity.toProduct
import com.mindmatrix.budakattusante.data.model.Product

class ProductPagingSource(
    private val productDao: ProductDao,
    private val query: String = "",
    private val category: String? = null
) : PagingSource<Int, Product>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            val page = params.key ?: 0
            val limit = params.loadSize
            val offset = page * limit
            
            val entities = if (query.isBlank() && category == null) {
                productDao.getProductsPaged(limit, offset)
            } else if (category != null) {
                productDao.getProductsByCategoryPaged(category, limit, offset)
            } else {
                productDao.searchProductsPaged(query, limit, offset)
            }

            val products = entities.map { it.toProduct() }

            LoadResult.Page(
                data = products,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (products.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

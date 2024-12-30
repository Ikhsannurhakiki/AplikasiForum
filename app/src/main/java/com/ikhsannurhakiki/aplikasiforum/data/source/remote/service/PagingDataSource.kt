
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.service.ApiService

class PagingDataSource(private val apiService: ApiService, private val suppLecturer:Int? ,private val code: Int?, private val key:String?, private val materialId: Int?) : PagingSource<Int, QuestionTagResponse>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuestionTagResponse> {

        return try {
            val position = params.key ?: 1
            val response = apiService.getAllQuestion("getAllQuestions", suppLecturer, code, key,materialId, position, 5)
            LoadResult.Page(
                data = response.body()!!.result,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response.code() != 200){
                    null
                }else{
                    position + 1
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }

    override fun getRefreshKey(state: PagingState<Int, QuestionTagResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}
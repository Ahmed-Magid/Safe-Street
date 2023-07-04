import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class ViewReportsViewModel(private val repository: Repository) : ViewModel() {
    val getLocationReportsResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun getLocationReports(token: String, id: Int, longitude: String, latitude: String){
        viewModelScope.launch {
            val response = repository.getLocationReports(token, id, longitude, latitude)
            getLocationReportsResponse.value = response
        }
    }
}
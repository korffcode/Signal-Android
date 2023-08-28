package com.app.signal.data.repository

import com.app.signal.data.dao.PhotoDaoFake
import com.app.signal.data.database.SignalDatabaseFake
import com.app.signal.data.dto.response.photo.ImageDto
import com.app.signal.data.json_serializer.UriSerializer
import com.app.signal.data.json_serializer.factory.SerializerConverterFactory
import com.app.signal.data.repository.photo.PhotoRepositoryImpl
import com.app.signal.data.repository.photo.store.PhotoLocalStore
import com.app.signal.data.repository.photo.store.PhotoRemoteStore
import com.app.signal.data.repository.store.PhotoRetrofitFake
import com.app.signal.data.repository.store.PhotoRoomFake
import com.app.signal.data.rest.api.MockPhotoApiResponse
import com.app.signal.data.rest.api.PhotoApi
import com.app.signal.data.room.entities.PhotoEntity
import com.app.signal.data.util.DataMediatorFake
import com.app.signal.data.util.FakeErrorHandler
import com.app.signal.domain.form.photo.SearchQueryParams
import com.app.signal.domain.model.PhotoListPage
import com.app.signal.domain.model.photo.Photo
import com.app.signal.domain.repository.PhotoRepository
import com.app.signal.domain.service.ErrorHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import java.net.HttpURLConnection

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepositoryTest {

    private val appDatabase = SignalDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    lateinit var gson: Gson

    private lateinit var localStore: PhotoLocalStore
    private lateinit var remoteStore: PhotoRemoteStore
    private lateinit var photoDao: PhotoDaoFake
    private lateinit var photoApi: PhotoApi
    private lateinit var photoRepository: PhotoRepository
    private lateinit var errorHandler: ErrorHandler

    private val scope = TestScope()

    val searchText = "lion"
    val page = 10000

    private val module = SerializersModule {
        contextual(UriSerializer)
    }

    @BeforeEach
    fun setup() {
        gson = GsonBuilder()
            .create()

        Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))

        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("")
        photoApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(SerializerConverterFactory(Json {
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                serializersModule = module
            }))
            .build()
            .create(PhotoApi::class.java)

        photoDao = PhotoDaoFake(appDatabase)
        errorHandler = FakeErrorHandler()
        localStore = PhotoRoomFake(photoDao)
        remoteStore = PhotoRetrofitFake(MockPhotoApiResponse)

        photoRepository =
            PhotoRepositoryImpl(
                DataMediatorFake(
                    errorHandler,
                    StandardTestDispatcher(scope.testScheduler)
                ), localStore, remoteStore
            )
    }

    @Test
    fun getSearchResultsTest(): Unit = runTest {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(MockPhotoApiResponse.photoResponse)
        )

        // run use case
        val photosAsFlow =
            photoRepository.getSearchResults(SearchQueryParams(searchText, page.toLong())).toList()

        // first emission should be `loading`
        assert(photosAsFlow[0].isLoading)
        // second should be correct data type
        assert(photosAsFlow[1].data is PhotoListPage<Photo>)
        assert(photosAsFlow[1].data?.items?.size == 25)
        assert(photosAsFlow[1].data?.items?.get(0)?.id == "52641319570")
    }

    @Test
    fun getSavedPhotosTest(): Unit = runTest {
        val savedPhotos = photoRepository.getSavedPhotos().toList()
        // First State is Loading
        assert(savedPhotos[0].isLoading)

        // Second State should be success
        assert(savedPhotos[1].isSuccess)

        // List should be empty
        assert(savedPhotos[1].data.isNullOrEmpty())

    }


    @Test
    fun savePhotoTest(): Unit = runTest {

        val savedPhotos = photoRepository.getSavedPhotos().toList()
        // First State is Loading
        assert(savedPhotos[0].isLoading)

        // Second State should be success
        assert(savedPhotos[1].isSuccess)

        // List should be empty
        assert(savedPhotos[1].data.isNullOrEmpty())

        // Add test entity to database
        val saveFlow = photoRepository.savePhoto(
            PhotoEntity(
                "testId",
                ImageDto(null, null, null),
                title = "test title",
                isFavourite = false
            )
        ).toList()

        assert(saveFlow[0].isLoading)
        assert(saveFlow[1].isSuccess)

        val nonEmptySavedPhotos = photoRepository.getSavedPhotos().toList()
        // First State is Loading
        assert(nonEmptySavedPhotos[0].isLoading)

        // Second State should be success
        assert(nonEmptySavedPhotos[1].isSuccess)

        // List should be empty
        assert(nonEmptySavedPhotos[1].data?.isNotEmpty() == true)

        assert(nonEmptySavedPhotos[1].data?.first()?.id == "testId" && nonEmptySavedPhotos[1].data?.first()?.title == "test title")

    }


    @Test
    fun removeSavedPhotoTest(): Unit = runTest {
        val savedPhotos = photoRepository.getSavedPhotos().toList()
        // First State is Loading
        assert(savedPhotos[0].isLoading)

        // Second State should be success
        assert(savedPhotos[1].isSuccess)

        // List should be empty
        assert(savedPhotos[1].data.isNullOrEmpty())

        // Add test entity to database
        val saveFlow = photoRepository.savePhoto(
            PhotoEntity(
                "testId",
                ImageDto(null, null, null),
                title = "test title",
                isFavourite = false
            )
        ).toList()

        assert(saveFlow[0].isLoading)
        assert(saveFlow[1].isSuccess)

        val nonEmptySavedPhotos = photoRepository.getSavedPhotos().toList()
        // First State is Loading
        assert(nonEmptySavedPhotos[0].isLoading)

        // Second State should be success
        assert(nonEmptySavedPhotos[1].isSuccess)

        // List should be empty
        assert(nonEmptySavedPhotos[1].data?.isNotEmpty() == true)

        assert(nonEmptySavedPhotos[1].data?.first()?.id == "testId" && nonEmptySavedPhotos[1].data?.first()?.title == "test title")

        // Delete

        val deleteFlow = photoRepository.removeSavedPhoto("testId").toList()

        // First State is Loading
        assert(deleteFlow[0].isLoading)

        // Second State should be success
        assert(deleteFlow[1].isSuccess)

        // List should be empty
        assert(nonEmptySavedPhotos[1].data?.isEmpty() == true)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
    }

    private fun <E> E.serialize(): String {
        return gson.toJson(this)
    }
}
package ru.hse.edu.placemarks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.domain.usecases.AddTagUseCase
import ru.hse.edu.placemarks.domain.usecases.DeletePlacemarkUseCase
import ru.hse.edu.placemarks.domain.usecases.DeleteTagUseCase
import ru.hse.edu.placemarks.domain.usecases.GetPlacemarksUseCase
import ru.hse.edu.placemarks.domain.usecases.GetTagsUseCase
import ru.hse.edu.placemarks.presentation.entities.SortType
import ru.hse.edu.placemarks.presentation.events.PlacemarkEvent
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.presentation.BaseViewModel
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PlacemarksViewModel @Inject constructor(
    private val getPlacemarksUseCase: GetPlacemarksUseCase,
    private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
) : BaseViewModel() {

    private val currentLatitude: Double = 55.7558
    private val currentLongitude: Double = 37.6173

    private val _rawPlacemarks =
        MutableStateFlow<ResultContainer<List<Placemark>>>(ResultContainer.Loading)

    private val _sortType = MutableStateFlow(SortType.BY_NAME_ASC)
    val sortType = _sortType.asStateFlow()

    private val _selectedTagIds = MutableStateFlow<List<Long>>(emptyList())
    val selectedTagIds = _selectedTagIds.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val placemarks: StateFlow<ResultContainer<List<Placemark>>> = combine(
        _rawPlacemarks, _sortType, _selectedTagIds, _searchQuery
    ) { container, sort, tagIds, query ->
        when (container) {
            is ResultContainer.Loading -> ResultContainer.Loading
            is ResultContainer.Error -> container
            is ResultContainer.Done -> {
                val filteredByTags = if (tagIds.isEmpty()) {
                    container.unwrap()
                } else {
                    container.unwrap().filter { placemark ->
                        placemark.tags.any { tag -> tag.id in tagIds }
                    }
                }
                val filteredByName = if (query.isBlank()) {
                    filteredByTags
                } else {
                    filteredByTags.filter { placemark ->
                        placemark.name.contains(query, ignoreCase = true)
                    }
                }
                val sorted = when (sort) {
                    SortType.BY_NAME_ASC -> filteredByName.sortedBy { it.name }
                    SortType.BY_NAME_DESC -> filteredByName.sortedByDescending { it.name }
                    SortType.BY_DISTANCE_ASC -> filteredByName.sortedBy {
                        calculateDistance(it.locationData.latitude, it.locationData.longitude)
                    }

                    SortType.BY_DISTANCE_DESC -> filteredByName.sortedByDescending {
                        calculateDistance(it.locationData.latitude, it.locationData.longitude)
                    }
                }
                ResultContainer.Done(sorted)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, ResultContainer.Loading)

    private val _tags =
        MutableStateFlow<ResultContainer<List<Tag>>>(ResultContainer.Loading)
    val tags = _tags.asStateFlow()

    init {
        collectPlacemarks()
        collectTags()
    }

    fun onEvent(event: PlacemarkEvent) {
        when (event) {
            is PlacemarkEvent.DeletePlacemark -> deletePlacemark(event.id)
            is PlacemarkEvent.DeleteTag -> deleteTag(event.id)
            is PlacemarkEvent.AddTag -> addTag(event.tag)
            is PlacemarkEvent.SortBy -> setSortType(event.sortType)
            is PlacemarkEvent.SelectTag -> selectTag(event.id)
            is PlacemarkEvent.SearchByName -> searchByName(event.query)
        }
    }

    private fun searchByName(query: String) {
        _searchQuery.value = query
    }

    private fun selectTag(tagId: Long) {
        if (_selectedTagIds.value.contains(tagId)) {
            _selectedTagIds.value = _selectedTagIds.value.filter { it != tagId }
        } else {
            _selectedTagIds.value += tagId
        }
    }

    private fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }

    private fun collectPlacemarks() = viewModelScope.launch {
        getPlacemarksUseCase.invoke().collect { result ->
            _rawPlacemarks.value = result
        }
    }

    private fun collectTags() = viewModelScope.launch {
        getTagsUseCase.invoke().collect { result ->
            _tags.value = result
        }
    }

    private fun deletePlacemark(id: Long) = viewModelScope.launch {
        deletePlacemarkUseCase.invoke(id)
    }

    private fun deleteTag(id: Long) = viewModelScope.launch {
        deleteTagUseCase.invoke(id)
    }

    private fun addTag(tag: Tag) = viewModelScope.launch {
        addTagUseCase.invoke(tag)
    }

    private fun calculateDistance(lat: Double, lon: Double): Double {
        val dLat = Math.toRadians(lat - currentLatitude)
        val dLon = Math.toRadians(lon - currentLongitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(currentLatitude)) *
                cos(Math.toRadians(lat)) *
                sin(dLon / 2).pow(2)
        return 6371.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val getPlacemarksUseCase: GetPlacemarksUseCase,
        private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
        private val getTagsUseCase: GetTagsUseCase,
        private val addTagUseCase: AddTagUseCase,
        private val deleteTagUseCase: DeleteTagUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PlacemarksViewModel::class.java)
            return PlacemarksViewModel(
                getPlacemarksUseCase = getPlacemarksUseCase,
                deletePlacemarkUseCase = deletePlacemarkUseCase,
                getTagsUseCase = getTagsUseCase,
                addTagUseCase = addTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
            ) as T
        }
    }
}
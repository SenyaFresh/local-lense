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
import ru.hse.locallense.common.entities.LocationProvider
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.common.entities.distanceMetersTo
import ru.hse.locallense.presentation.BaseViewModel
import javax.inject.Inject

class PlacemarksViewModel @Inject constructor(
    private val getPlacemarksUseCase: GetPlacemarksUseCase,
    private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val locationProvider: LocationProvider,
) : BaseViewModel() {

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
            is ResultContainer.Loading -> container
            is ResultContainer.Error -> container
            is ResultContainer.Done -> ResultContainer.Done(
                container.value
                    .filterByTags(tagIds)
                    .filterByName(query)
                    .sortBy(sort)
            )
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
            is PlacemarkEvent.SortBy -> _sortType.value = event.sortType
            is PlacemarkEvent.SelectTag -> toggleTag(event.id)
            is PlacemarkEvent.SearchByName -> _searchQuery.value = event.query
        }
    }

    private fun toggleTag(tagId: Long) {
        _selectedTagIds.value = if (tagId in _selectedTagIds.value) {
            _selectedTagIds.value - tagId
        } else {
            _selectedTagIds.value + tagId
        }
    }

    private fun List<Placemark>.filterByTags(tagIds: List<Long>): List<Placemark> =
        if (tagIds.isEmpty()) this
        else filter { placemark -> placemark.tags.any { it.id in tagIds } }

    private fun List<Placemark>.filterByName(query: String): List<Placemark> =
        if (query.isBlank()) this
        else filter { it.name.contains(query, ignoreCase = true) }

    private fun List<Placemark>.sortBy(sort: SortType): List<Placemark> = when (sort) {
        SortType.BY_NAME_ASC -> sortedBy { it.name }
        SortType.BY_NAME_DESC -> sortedByDescending { it.name }
        SortType.BY_DISTANCE_ASC -> sortedByDistance(ascending = true)
        SortType.BY_DISTANCE_DESC -> sortedByDistance(ascending = false)
    }

    private fun List<Placemark>.sortedByDistance(ascending: Boolean): List<Placemark> {
        val origin = locationProvider.current ?: return sortedBy { it.name }
        return if (ascending) sortedBy { origin.distanceMetersTo(it.locationData) }
        else sortedByDescending { origin.distanceMetersTo(it.locationData) }
    }

    private fun collectPlacemarks() = viewModelScope.launch {
        getPlacemarksUseCase.invoke().collect { _rawPlacemarks.value = it }
    }

    private fun collectTags() = viewModelScope.launch {
        getTagsUseCase.invoke().collect { _tags.value = it }
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

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val getPlacemarksUseCase: GetPlacemarksUseCase,
        private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
        private val getTagsUseCase: GetTagsUseCase,
        private val addTagUseCase: AddTagUseCase,
        private val deleteTagUseCase: DeleteTagUseCase,
        private val locationProvider: LocationProvider,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PlacemarksViewModel::class.java)
            return PlacemarksViewModel(
                getPlacemarksUseCase = getPlacemarksUseCase,
                deletePlacemarkUseCase = deletePlacemarkUseCase,
                getTagsUseCase = getTagsUseCase,
                addTagUseCase = addTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
                locationProvider = locationProvider,
            ) as T
        }
    }
}

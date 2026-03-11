package ru.hse.edu.placemarks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.domain.usecases.AddTagUseCase
import ru.hse.edu.placemarks.domain.usecases.DeletePlacemarkUseCase
import ru.hse.edu.placemarks.domain.usecases.DeleteTagUseCase
import ru.hse.edu.placemarks.domain.usecases.GetPlacemarksUseCase
import ru.hse.edu.placemarks.domain.usecases.GetTagsUseCase
import ru.hse.edu.placemarks.presentation.events.PlacemarkEvent
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.presentation.BaseViewModel
import javax.inject.Inject
import kotlin.jvm.java

class PlacemarksViewModel @Inject constructor(
    private val getPlacemarksUseCase: GetPlacemarksUseCase,
    private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
): BaseViewModel() {

    private val _placemarks =
        MutableStateFlow<ResultContainer<List<Placemark>>>(ResultContainer.Loading)
    val placemarks = _placemarks.asStateFlow()

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
        }
    }

    private fun collectPlacemarks() = viewModelScope.launch {
        getPlacemarksUseCase.invoke().collect { result ->
            _placemarks.value = result
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
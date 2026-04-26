package ru.hse.edu.ar.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.domain.usecases.AddPlacemarkUseCase
import ru.hse.edu.ar.domain.usecases.AddTagUseCase
import ru.hse.edu.ar.domain.usecases.DeletePlacemarkUseCase
import ru.hse.edu.ar.domain.usecases.DeleteTagUseCase
import ru.hse.edu.ar.domain.usecases.GetPlacemarksUseCase
import ru.hse.edu.ar.domain.usecases.GetTagsUseCase
import ru.hse.edu.ar.presentation.events.PlacemarkEvent
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.presentation.BaseViewModel
import javax.inject.Inject

class ArViewModel @Inject constructor(
    private val getPlacemarksUseCase: GetPlacemarksUseCase,
    private val addPlacemarkUseCase: AddPlacemarkUseCase,
    private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
) : BaseViewModel() {

    private val _placemarks =
        MutableStateFlow<ResultContainer<List<ArPlacemark>>>(ResultContainer.Loading)
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
            is PlacemarkEvent.AddPlacemark -> addPlacemark(event.placemark)
            is PlacemarkEvent.DeletePlacemark -> deletePlacemark(event.id)
            is PlacemarkEvent.AddTag -> addTag(event.tag)
            is PlacemarkEvent.DeleteTag -> deleteTag(event.id)
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

    private fun addPlacemark(placemark: ArPlacemark) = viewModelScope.launch {
        addPlacemarkUseCase.invoke(placemark)
    }

    private fun deletePlacemark(id: Long) = viewModelScope.launch {
        deletePlacemarkUseCase.invoke(id)
    }

    private fun addTag(tag: Tag) = viewModelScope.launch {
        addTagUseCase.invoke(tag)
    }

    private fun deleteTag(id: Long) = viewModelScope.launch {
        deleteTagUseCase.invoke(id)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val getPlacemarksUseCase: GetPlacemarksUseCase,
        private val addPlacemarkUseCase: AddPlacemarkUseCase,
        private val deletePlacemarkUseCase: DeletePlacemarkUseCase,
        private val getTagsUseCase: GetTagsUseCase,
        private val addTagUseCase: AddTagUseCase,
        private val deleteTagUseCase: DeleteTagUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == ArViewModel::class.java)
            return ArViewModel(
                getPlacemarksUseCase = getPlacemarksUseCase,
                addPlacemarkUseCase = addPlacemarkUseCase,
                deletePlacemarkUseCase = deletePlacemarkUseCase,
                getTagsUseCase = getTagsUseCase,
                addTagUseCase = addTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
            ) as T
        }
    }
}
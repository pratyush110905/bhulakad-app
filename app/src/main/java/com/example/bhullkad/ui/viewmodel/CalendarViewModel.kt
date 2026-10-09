package com.example.bhullkad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bhullkad.data.entity.CalendarEvent
import com.example.bhullkad.data.repository.CalendarEventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(private val repository: CalendarEventRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val viewedMonth: StateFlow<LocalDate> = _viewedMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForSelectedDate: StateFlow<List<CalendarEvent>> = _selectedDate
        .flatMapLatest { date -> repository.eventsForDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForViewedMonth: StateFlow<List<CalendarEvent>> = _viewedMonth
        .flatMapLatest { monthDate ->
            val prefix = monthDate.toString().substring(0, 7)
            repository.eventsForMonth(prefix)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setViewedMonth(date: LocalDate) {
        _viewedMonth.value = date.withDayOfMonth(1)
    }

    fun addEvent(title: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            repository.insert(
                CalendarEvent(
                    title = title,
                    description = description,
                    date = date.toString()
                )
            )
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch { repository.delete(event) }
    }

    class Factory(private val repository: CalendarEventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}

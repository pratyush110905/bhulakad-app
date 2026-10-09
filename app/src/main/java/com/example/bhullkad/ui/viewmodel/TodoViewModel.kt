package com.example.bhullkad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bhullkad.data.entity.Task
import com.example.bhullkad.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TaskRepository) : ViewModel() {

    val activeTasks: StateFlow<List<Task>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedTasks: StateFlow<List<Task>> = repository.archivedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String) {
        viewModelScope.launch { repository.addTask(title) }
    }

    fun toggleTaskCompletion(task: Task, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(task, completed) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearArchive() }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodoViewModel(repository) as T
        }
    }
}

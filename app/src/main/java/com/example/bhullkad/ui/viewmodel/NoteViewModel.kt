package com.example.bhullkad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bhullkad.data.entity.Note
import com.example.bhullkad.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveNote(id: Long, title: String, content: String) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.insert(Note(title = title, content = content))
            } else {
                val existing = repository.getById(id)
                if (existing != null) {
                    repository.update(existing.copy(
                        title = title, 
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.delete(note) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(repository) as T
        }
    }
}

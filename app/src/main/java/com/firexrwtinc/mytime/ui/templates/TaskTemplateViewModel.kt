package com.firexrwtinc.mytime.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.TaskTemplate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskTemplateViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val templateDao = database.taskTemplateDao()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Получаем шаблоны с учетом поиска
    val templates: StateFlow<List<TaskTemplate>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                templateDao.getAllTemplates()
            } else {
                templateDao.searchTemplates(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun createTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateDao.insertTemplate(template.copy(
                    updatedAt = System.currentTimeMillis()
                ))
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при создании шаблона: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateDao.updateTemplate(template.copy(
                    updatedAt = System.currentTimeMillis()
                ))
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении шаблона: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateDao.deleteTemplate(template)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении шаблона: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTemplateById(templateId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateDao.deleteTemplateById(templateId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении шаблона: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun getTemplateById(templateId: Long): TaskTemplate? {
        return try {
            templateDao.getTemplateById(templateId)
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка при получении шаблона: ${e.message}"
            null
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
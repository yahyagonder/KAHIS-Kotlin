package com.yahyagonder.airquality.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahyagonder.airquality.data.SensorData
import com.yahyagonder.airquality.data.SharedLocationData
import com.yahyagonder.airquality.repository.FirebaseSensorRepository
import com.yahyagonder.airquality.service.SensorMonitoringService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: FirebaseSensorRepository = FirebaseSensorRepository()
) : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _sharedLocations = MutableStateFlow<List<SharedLocationData>>(emptyList())
    val sharedLocations: StateFlow<List<SharedLocationData>> = _sharedLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected.asStateFlow()

    init {
        fetchSensorData()
        fetchSharedLocations()
    }

    fun setWifiConnected(connected: Boolean, context: Context) {
        _isWifiConnected.value = connected
        if (connected) {
            startMonitoringService(context)
        } else {
            stopMonitoringService(context)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean, context: Context) {
        _notificationsEnabled.value = enabled
        if (enabled) {
            if (_isWifiConnected.value) {
                startMonitoringService(context)
            }
        } else {
            stopMonitoringService(context)
        }
    }

    fun startMonitoringService(context: Context) {
        if (!_notificationsEnabled.value || !_isWifiConnected.value) return
        val intent = Intent(context, SensorMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopMonitoringService(context: Context) {
        val intent = Intent(context, SensorMonitoringService::class.java)
        context.stopService(intent)
    }

    private fun fetchSensorData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSensorDataFlow()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { data ->
                    _sensorData.value = data
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }

    private fun fetchSharedLocations() {
        viewModelScope.launch {
            repository.getSharedLocationsFlow()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { data ->
                    _sharedLocations.value = data
                }
        }
    }
    
    fun shareLocation(latitude: Double, longitude: Double, pm25: Int, voc: Int, uv: Int, sharedBy: String, userId: String) {
        val data = SharedLocationData(
            latitude = latitude,
            longitude = longitude,
            pm25 = pm25,
            voc = voc,
            uv = uv,
            sharedBy = sharedBy,
            userId = userId,
            timestamp = System.currentTimeMillis()
        )
        repository.pushSharedLocation(data)
    }

    fun deleteMeasurement(id: String) {
        repository.deleteSharedLocation(id)
    }

    fun resetState() {
        _sensorData.value = null
        _sharedLocations.value = emptyList()
        _error.value = null
        _notificationsEnabled.value = false
        _isWifiConnected.value = false
    }
}

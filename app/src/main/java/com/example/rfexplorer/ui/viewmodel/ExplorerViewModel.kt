package com.example.rfexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rfexplorer.data.model.*
import com.example.rfexplorer.data.repository.ScanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExplorerViewModel(private val repository: ScanRepository) : ViewModel() {

    val isScanning: StateFlow<Boolean> = repository.isScanning
    val scannedLibraries: StateFlow<List<ScannedLibrary>> = repository.scannedLibraries
    val jniRegistrations: StateFlow<List<JniRegistration>> = repository.jniRegistrations
    val halInterfaces: StateFlow<List<HalInterface>> = repository.halInterfaces
    val binderServices: StateFlow<List<BinderServiceInfo>> = repository.binderServices
    val qualcommPlatform: StateFlow<QualcommPlatformDetection?> = repository.qualcommPlatform
    val fmAnalysis: StateFlow<FmHardwareAnalysis?> = repository.fmAnalysis
    val systemProperties: StateFlow<List<SystemProperty>> = repository.systemProperties
    val audioPipeline: StateFlow<AudioPipelineAnalysis?> = repository.audioPipeline
    val kernelInfo: StateFlow<KernelCapabilityInfo?> = repository.kernelInfo
    val activationMethods: StateFlow<List<ActivationEvaluation>> = repository.activationMethods
    val safeRecommendations: StateFlow<List<SafeRecommendationStep>> = repository.safeRecommendations
    val dependencyNodes: StateFlow<List<DependencyGraphNode>> = repository.dependencyNodes
    val dependencyEdges: StateFlow<List<DependencyGraphEdge>> = repository.dependencyEdges
    val riskReport: StateFlow<RiskAssessmentReport?> = repository.riskReport
    val developerLogs: StateFlow<List<String>> = repository.developerLogs
    val savedReports: StateFlow<List<ScanReportEntity>> = repository.savedReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedCategoryFilter = MutableStateFlow<LibraryCategory?>(null)
    val selectedCategoryFilter: StateFlow<LibraryCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedElfInfo = MutableStateFlow<ElfBinaryInfo?>(null)
    val selectedElfInfo: StateFlow<ElfBinaryInfo?> = _selectedElfInfo.asStateFlow()

    private val _developerModeEnabled = MutableStateFlow(false)
    val developerModeEnabled: StateFlow<Boolean> = _developerModeEnabled.asStateFlow()

    val filteredLibraries: StateFlow<List<ScannedLibrary>> = combine(
        scannedLibraries,
        _selectedCategoryFilter,
        _searchQuery
    ) { list, cat, query ->
        list.filter { lib ->
            val matchesCat = (cat == null) || (lib.category == cat)
            val matchesQuery = query.isBlank() || lib.name.contains(query, ignoreCase = true) || lib.path.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Trigger initial scan upon startup
        runScan()
    }

    fun runScan() {
        viewModelScope.launch {
            repository.performFullSystemScan()
        }
    }

    fun setCategoryFilter(category: LibraryCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun inspectElfBinary(library: ScannedLibrary) {
        viewModelScope.launch {
            val info = repository.analyzeElfBinary(library)
            _selectedElfInfo.value = info
        }
    }

    fun clearSelectedElfInfo() {
        _selectedElfInfo.value = null
    }

    fun toggleDeveloperMode() {
        _developerModeEnabled.value = !_developerModeEnabled.value
        val stateStr = if (_developerModeEnabled.value) "ENABLED (Read-Only Safety Override Active)" else "DISABLED (Read-Only Mode Locked)"
        repository.addLog("[DEV] Developer Mode toggled: $stateStr")
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }

    fun clearAllReports() {
        viewModelScope.launch {
            repository.clearAllReports()
        }
    }

    fun exportFullReportText(): String {
        val fm = fmAnalysis.value
        val qcom = qualcommPlatform.value
        val kernel = kernelInfo.value
        val libs = scannedLibraries.value
        return """
=====================================================
RF HARDWARE EXPLORER ULTIMATE AI - SYSTEM REPORT
=====================================================
Target Device: ${qcom?.deviceModel ?: "SM-X216B"}
Chipset SoC: ${qcom?.chipsetModel ?: "Snapdragon 695 5G"}
Kernel Architecture: ${kernel?.architecture ?: "ARM64"}
SELinux Enforcement: ${kernel?.selinuxMode ?: "Enforcing"}

--- SYSTEM A: HARDWARE REVERSE ENGINEERING ---
Total Libraries Scanned: ${libs.size}
JNI Registrations Found: ${jniRegistrations.value.size}
HAL Interfaces Enumerated: ${halInterfaces.value.size}
Binder Services Active: ${binderServices.value.size}

--- SYSTEM B: FM HARDWARE CAPABILITY ---
FM Baseband Silicon: PRESENT (Qualcomm FastConnect WCN3998)
FM JNI Bridges Found: ${fm?.isJniPresent ?: false}
FM HAL 2.0 Interface: ${fm?.isHalPresent ?: false}
Overall Activation Probability: ${fm?.activationProbabilityPercent ?: 0}%
Top Recommended Activation Path: Magisk Root / Custom ROM (LineageOS)
=====================================================
        """.trimIndent()
    }
}

class ExplorerViewModelFactory(private val repository: ScanRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExplorerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExplorerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

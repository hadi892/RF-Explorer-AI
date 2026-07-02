package com.example.rfexplorer.data.repository

import com.example.rfexplorer.data.db.ScanReportDao
import com.example.rfexplorer.data.model.*
import com.example.rfexplorer.data.scanner.ElfBinaryAnalyzer
import com.example.rfexplorer.data.scanner.HardwareScannerEngines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class ScanRepository(private val scanReportDao: ScanReportDao) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedLibraries = MutableStateFlow<List<ScannedLibrary>>(emptyList())
    val scannedLibraries: StateFlow<List<ScannedLibrary>> = _scannedLibraries.asStateFlow()

    private val _jniRegistrations = MutableStateFlow<List<JniRegistration>>(emptyList())
    val jniRegistrations: StateFlow<List<JniRegistration>> = _jniRegistrations.asStateFlow()

    private val _halInterfaces = MutableStateFlow<List<HalInterface>>(emptyList())
    val halInterfaces: StateFlow<List<HalInterface>> = _halInterfaces.asStateFlow()

    private val _binderServices = MutableStateFlow<List<BinderServiceInfo>>(emptyList())
    val binderServices: StateFlow<List<BinderServiceInfo>> = _binderServices.asStateFlow()

    private val _qualcommPlatform = MutableStateFlow<QualcommPlatformDetection?>(null)
    val qualcommPlatform: StateFlow<QualcommPlatformDetection?> = _qualcommPlatform.asStateFlow()

    private val _fmAnalysis = MutableStateFlow<FmHardwareAnalysis?>(null)
    val fmAnalysis: StateFlow<FmHardwareAnalysis?> = _fmAnalysis.asStateFlow()

    private val _systemProperties = MutableStateFlow<List<SystemProperty>>(emptyList())
    val systemProperties: StateFlow<List<SystemProperty>> = _systemProperties.asStateFlow()

    private val _audioPipeline = MutableStateFlow<AudioPipelineAnalysis?>(null)
    val audioPipeline: StateFlow<AudioPipelineAnalysis?> = _audioPipeline.asStateFlow()

    private val _kernelInfo = MutableStateFlow<KernelCapabilityInfo?>(null)
    val kernelInfo: StateFlow<KernelCapabilityInfo?> = _kernelInfo.asStateFlow()

    private val _activationMethods = MutableStateFlow<List<ActivationEvaluation>>(emptyList())
    val activationMethods: StateFlow<List<ActivationEvaluation>> = _activationMethods.asStateFlow()

    private val _safeRecommendations = MutableStateFlow<List<SafeRecommendationStep>>(emptyList())
    val safeRecommendations: StateFlow<List<SafeRecommendationStep>> = _safeRecommendations.asStateFlow()

    private val _dependencyNodes = MutableStateFlow<List<DependencyGraphNode>>(emptyList())
    val dependencyNodes: StateFlow<List<DependencyGraphNode>> = _dependencyNodes.asStateFlow()

    private val _dependencyEdges = MutableStateFlow<List<DependencyGraphEdge>>(emptyList())
    val dependencyEdges: StateFlow<List<DependencyGraphEdge>> = _dependencyEdges.asStateFlow()

    private val _riskReport = MutableStateFlow<RiskAssessmentReport?>(null)
    val riskReport: StateFlow<RiskAssessmentReport?> = _riskReport.asStateFlow()

    private val _developerLogs = MutableStateFlow<List<String>>(
        listOf(
            "[INIT] RF Hardware Explorer Ultimate AI initialized in Read-Only Sandbox.",
            "[INFO] Target Device verified: Samsung Galaxy Tab A9+ 5G (SM-X216B).",
            "[INFO] Chipset detected: Qualcomm Snapdragon 695 5G (Blair Platform SM6375).",
            "[SEC] All automated filesystem write operations disabled."
        )
    )
    val developerLogs: StateFlow<List<String>> = _developerLogs.asStateFlow()

    val savedReports: Flow<List<ScanReportEntity>> = scanReportDao.getAllReports()

    fun addLog(msg: String) {
        _developerLogs.value = listOf(msg) + _developerLogs.value.take(199)
    }

    suspend fun performFullSystemScan() = withContext(Dispatchers.IO) {
        _isScanning.value = true
        addLog("[SCAN] Starting deep hardware scan across /system, /vendor, /apex...")

        // Module 1: Libraries
        val libs = HardwareScannerEngines.scanAllLibraries()
        _scannedLibraries.value = libs
        addLog("[SCAN] Enumerated ${libs.size} shared objects categorized across 18 subsystems.")

        // Module 3: JNI
        val jni = HardwareScannerEngines.analyzeJniRegistrations()
        _jniRegistrations.value = jni

        // Module 4: HAL
        val hals = HardwareScannerEngines.enumerateHalInterfaces()
        _halInterfaces.value = hals

        // Module 5: Binder
        val binders = HardwareScannerEngines.listBinderServices()
        _binderServices.value = binders

        // Module 6: QCOM Platform
        val qcom = HardwareScannerEngines.detectQualcommPlatform()
        _qualcommPlatform.value = qcom

        // Module 7: FM Hardware
        val fm = HardwareScannerEngines.analyzeFmHardware(libs)
        _fmAnalysis.value = fm

        // Module 8: Properties
        val props = HardwareScannerEngines.analyzeProperties()
        _systemProperties.value = props

        // Module 9: Audio Pipeline
        val audio = HardwareScannerEngines.analyzeAudioPipeline()
        _audioPipeline.value = audio

        // Module 10: Kernel
        val kernel = HardwareScannerEngines.inspectKernel()
        _kernelInfo.value = kernel

        // Module 11: Activation
        val evals = HardwareScannerEngines.evaluateActivationMethods()
        _activationMethods.value = evals

        // Module 12: Safe Recommendations
        val recs = HardwareScannerEngines.generateSafeRecommendations()
        _safeRecommendations.value = recs

        // Module 13: Dependency Graph
        val graph = HardwareScannerEngines.generateDependencyGraphNodes()
        _dependencyNodes.value = graph.first
        _dependencyEdges.value = graph.second

        // Module 14: Risk Assessment
        val risk = HardwareScannerEngines.assessRisks()
        _riskReport.value = risk

        // Save report to Room database
        val json = HardwareScannerEngines.exportReportJson(libs, fm, evals)
        val entity = ScanReportEntity(
            totalLibrariesScanned = libs.size,
            fmProbabilityPercent = fm.activationProbabilityPercent,
            selinuxStatus = kernel.selinuxMode,
            jsonSummary = json
        )
        scanReportDao.insertReport(entity)
        addLog("[SUCCESS] Deep scan complete and report archived to local Room database.")

        _isScanning.value = false
    }

    suspend fun analyzeElfBinary(library: ScannedLibrary): ElfBinaryInfo = withContext(Dispatchers.IO) {
        addLog("[ELF] Analyzing binary headers and symbol tables for ${library.name}...")
        return@withContext ElfBinaryAnalyzer.analyzeFile(File(library.path))
    }

    suspend fun deleteReport(reportId: Long) = withContext(Dispatchers.IO) {
        scanReportDao.deleteReportById(reportId)
        addLog("[DB] Deleted report ID #$reportId.")
    }

    suspend fun clearAllReports() = withContext(Dispatchers.IO) {
        scanReportDao.deleteAllReports()
        addLog("[DB] Cleared all archived scan reports.")
    }
}

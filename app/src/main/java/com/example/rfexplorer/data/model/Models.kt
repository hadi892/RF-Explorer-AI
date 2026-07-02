package com.example.rfexplorer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LibraryCategory(val displayName: String, val iconName: String) {
    FM("FM Radio & Tuning", "radio"),
    RF("RF & Transceiver", "cell_tower"),
    AUDIO("Audio Routing & Mixer", "volume_up"),
    DSP("Qualcomm Hexagon / ADSP", "memory"),
    MODEM("Cellular Baseband / QMI", "signal_cellular_alt"),
    BINDER("IPC & Binder Daemons", "swap_horiz"),
    HAL("Hardware Abstraction Layer", "hardware"),
    JNI("Java Native Interface", "code"),
    SENSORS("Sensors & Telemetry", "sensors"),
    BLUETOOTH("Bluetooth Stack", "bluetooth"),
    WIFI("Wi-Fi & WLAN", "wifi"),
    USB("USB OTG / Gadget", "usb"),
    CAMERA("Camera & ISP", "camera_alt"),
    CODEC("Media & Codecs", "movie"),
    GPU("Adreno / Graphics", "developer_board"),
    SECURITY("TrustZone / QSEE / Keymaster", "security"),
    VENDOR("Vendor Custom Framework", "business"),
    OTHER("System Library", "folder")
}

data class ScannedLibrary(
    val path: String,
    val name: String,
    val category: LibraryCategory,
    val sizeBytes: Long,
    val architecture: String = "ARM64",
    val isReadable: Boolean = true,
    val sha256Prefix: String = ""
)

data class ElfBinaryInfo(
    val filePath: String,
    val fileName: String,
    val soname: String,
    val architecture: String,
    val is64Bit: Boolean,
    val exportedSymbols: List<String>,
    val importedSymbols: List<String>,
    val neededDependencies: List<String>,
    val sections: List<String>,
    val jniMethods: List<String>,
    val hiddenSymbolsCount: Int,
    val rpath: String? = null
)

data class JniRegistration(
    val javaClassName: String,
    val javaMethodName: String,
    val nativeSymbolName: String,
    val libraryPath: String,
    val likelyHalTarget: String,
    val likelyKernelNode: String
)

enum class HalType {
    AIDL, HIDL, BINDER, VENDOR_BINDER
}

data class HalInterface(
    val name: String,
    val type: HalType,
    val versionOrInstance: String,
    val isRunning: Boolean,
    val clientCount: Int = 0,
    val category: LibraryCategory
)

data class BinderServiceInfo(
    val serviceName: String,
    val interfaceName: String,
    val isVendorService: Boolean,
    val isHiddenSystem: Boolean,
    val estimatedTransactionCodes: Int,
    val requiredPermission: String?
)

data class QualcommPlatformDetection(
    val chipsetModel: String = "Snapdragon 695 5G (Blair Platform SM6375)",
    val deviceModel: String = "Samsung Galaxy Tab A9+ 5G (SM-X216B)",
    val isQmiPresent: Boolean,
    val isDiagPresent: Boolean,
    val isAdspPresent: Boolean,
    val isCdspPresent: Boolean,
    val isSlpiPresent: Boolean,
    val isTrustZonePresent: Boolean,
    val qmiDeviceNodes: List<String>,
    val vendorDaemonsDetected: List<String>
)

data class FmHardwareAnalysis(
    val detectedLibraries: List<ScannedLibrary>,
    val isHalPresent: Boolean,
    val isJniPresent: Boolean,
    val isVendorServicePresent: Boolean,
    val isAudioRoutePresent: Boolean,
    val isKernelSupportPresent: Boolean,
    val activationProbabilityPercent: Int,
    val confidenceLevel: String, // "High", "Medium", "Low"
    val blockingFactors: List<String>,
    val positiveIndicators: List<String>
)

data class SystemProperty(
    val key: String,
    val value: String,
    val sourceCategory: String // "QCOM", "AUDIO", "FM", "RADIO", "HAL", "SYS"
)

data class AudioRouteInfo(
    val name: String,
    val type: String, // "TUNER", "MIXER", "OFFLOAD", "OUTPUT"
    val isFmCapable: Boolean,
    val details: String
)

data class AudioPipelineAnalysis(
    val audioPolicyExists: Boolean,
    val mixerPathsExists: Boolean,
    val fmAudioPathsFound: Int,
    val dspOffloadSupported: Boolean,
    val discoveredRoutes: List<AudioRouteInfo>
)

data class KernelCapabilityInfo(
    val kernelVersionString: String,
    val architecture: String,
    val selinuxMode: String, // "Enforcing", "Permissive", "Disabled"
    val exposedDeviceNodes: List<String>,
    val radioDriversDetected: List<String>
)

enum class ActivationMethodType {
    OFFICIAL_ANDROID_API,
    HIDDEN_FRAMEWORK_API,
    JAVA_REFLECTION,
    BINDER_IPC_DIRECT,
    JNI_DIRECT_TUNNEL,
    SAMSUNG_VENDOR_SERVICE,
    CUSTOM_ROM_REQUIRED,
    ROOT_REQUIRED,
    KERNEL_SELINUX_MOD
}

data class ActivationEvaluation(
    val method: ActivationMethodType,
    val displayName: String,
    val probabilityPercent: Int,
    val feasibilityStatus: String, // "Plausible", "Restricted by SELinux", "Missing Driver", "Requires Root"
    val technicalRationale: String,
    val blockers: List<String>
)

data class SafeRecommendationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val riskLevel: String, // "SAFE (Read-Only)", "LOW", "MEDIUM", "HIGH"
    val privilegeRequired: String // "Standard App", "ADB / Shell", "Root", "Vendor/OEM Sign"
)

data class DependencyGraphNode(
    val id: String,
    val label: String,
    val type: String, // "LIB", "HAL", "JNI", "HW"
    val category: LibraryCategory
)

data class DependencyGraphEdge(
    val fromId: String,
    val toId: String,
    val relationship: String
)

data class RiskItem(
    val category: String,
    val title: String,
    val severity: String, // "LOW", "MODERATE", "CRITICAL"
    val description: String,
    val mitigation: String
)

data class RiskAssessmentReport(
    val securityRiskScore: Int, // 0-100
    val systemStabilityRisk: Int,
    val warrantyImpact: String,
    val compatibilitySummary: String,
    val recoveryDifficulty: String,
    val detailedRisks: List<RiskItem>
)

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val targetDevice: String = "SM-X216B (Galaxy Tab A9+ 5G)",
    val totalLibrariesScanned: Int,
    val fmProbabilityPercent: Int,
    val selinuxStatus: String,
    val jsonSummary: String
)

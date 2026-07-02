package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.rfexplorer.data.db.RfExplorerDatabase
import com.example.rfexplorer.data.repository.ScanRepository
import com.example.rfexplorer.ui.navigation.MainNavigationScaffold
import com.example.rfexplorer.ui.theme.CyberObsidian
import com.example.rfexplorer.ui.theme.RfExplorerTheme
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExplorerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Clean Repository
        val db = RfExplorerDatabase.getDatabase(applicationContext)
        val repository = ScanRepository(db.scanReportDao())
        val factory = ExplorerViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExplorerViewModel::class.java]

        setContent {
            RfExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberObsidian
                ) {
                    MainNavigationScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

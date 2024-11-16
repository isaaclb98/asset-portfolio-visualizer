package com.example.assetportfoliovisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.assetportfoliovisualizer.ui.theme.AssetPortfolioVisualizerTheme


class MainActivity : ComponentActivity() {
    // Room database
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "asset_database"
        ).build()

        // Initialize our ViewModels
        val tickerSearchViewModel = TickerSearchViewModel()
        val ownedAssetsViewModel = OwnedAssetsViewModel(db)
        val timeSeriesViewModel = TimeSeriesViewModel()

        enableEdgeToEdge()
        setContent {
            AssetPortfolioVisualizerTheme {
                MyAppScreen(tickerSearchViewModel, ownedAssetsViewModel, timeSeriesViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppScreen(tickerSearchViewModel: TickerSearchViewModel, ownedAssetsViewModel: OwnedAssetsViewModel, timeSeriesViewModel: TimeSeriesViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { TitleComponent() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.pastel_blue)
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Add assets section
                SectionTitle(stringResource(id = R.string.section_title_add_assets))
                // Search field
                TickerSearchField(tickerSearchViewModel)
                // Get the search results LiveData from our view model
                val searchResults by tickerSearchViewModel.searchResults.observeAsState(emptyList())
                // Display the search results
                LazyColumn {
                    items(searchResults) {
                        result -> SearchResultItem(
                            result = result,
                            onItemClick = {
                                val asset = OwnedAsset(symbol = result.symbol.toString(), name = result.name.toString(), type = result.type.toString(), region = result.region.toString())
                                ownedAssetsViewModel.addOwnedAsset(asset)
                            }
                        )
                    }
                }

                // Owned assets section
                SectionTitle(stringResource(id = R.string.owned_assets))
                // Get our owned assets from our view model
                val ownedAssets by ownedAssetsViewModel.ownedAssets.observeAsState(emptyList())
                // Display our owned assets
                LazyColumn {
                    items(ownedAssets) { asset ->
                        OwnedAssetItem(
                            asset,
                            onClickDelete = { asset -> ownedAssetsViewModel.deleteOwnedAsset(asset) }
                        )
                    }
                }

                // Update button
                UpdateButton( onClickUpdate = { timeSeriesViewModel.fetchTimeSeriesForAssets(ownedAssets) })
            }
        }
    )
}

@Preview
@Composable
fun TitleComponent() {
    Text(
        text = stringResource(id = R.string.app_name),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

@Composable
fun TickerSearchField(viewModel: TickerSearchViewModel) {
    var ticker by remember { mutableStateOf("") }

    OutlinedTextField(
        value = ticker,
        onValueChange = {
            ticker = it
        },
        label = { Text(stringResource(id = R.string.search_asset)) },
        placeholder = { Text(stringResource(id = R.string.search_asset_default)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (ticker.isNotBlank()) {
                    viewModel.searchForSymbols(ticker)
                }
            }
        )
    )
}

@Composable
fun SearchResultItem(result: BestMatch, onItemClick: (BestMatch) -> Unit) {
    Text(
        text = "${result.symbol} - ${result.name}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onItemClick(result) }
        )
}

@Composable
fun OwnedAssetItem(asset: OwnedAsset, onClickDelete: (OwnedAsset) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Delete icon
        IconButton(
            onClick = { onClickDelete(asset) },
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete asset",
                tint = androidx.compose.ui.graphics.Color.Red
            )
        }
        Text(
            text = "${asset.symbol} - ${asset.name} (${asset.type}, ${asset.region})",
        )
    }
}

@Composable
fun UpdateButton(onClickUpdate: () -> Unit) {
    FilledTonalButton(
        onClick = { onClickUpdate() },
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text("Update")
    }
}

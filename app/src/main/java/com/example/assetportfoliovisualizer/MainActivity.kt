package com.example.assetportfoliovisualizer

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import co.yml.charts.common.components.Legends
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.utils.DataUtils
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.assetportfoliovisualizer.ui.theme.AssetPortfolioVisualizerTheme
import kotlin.random.Random


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
            val searchResults by tickerSearchViewModel.searchResults.observeAsState(emptyList())
            val ownedAssets by ownedAssetsViewModel.ownedAssets.observeAsState(emptyList())
            // symbol -> (price * quantity)
            val assetHoldingTotalValues by timeSeriesViewModel.assetHoldingTotalValues.observeAsState(emptyMap())
            val netWorth by timeSeriesViewModel.netWorth.observeAsState()
            var isUpdateTriggered by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    // Add assets section
                    SectionTitle(stringResource(id = R.string.section_title_add_assets))
                }

                item {
                    // Search field
                    TickerSearchField(tickerSearchViewModel)
                }

                // Get the search results LiveData from our view model
                // Display the search results
                if (searchResults.isNotEmpty()) {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onAddAsset = { asset -> ownedAssetsViewModel.addOwnedAsset(asset) }
                        )
                    }
                }

                item {
                    SectionTitle(stringResource(id = R.string.owned_assets))
                }

                // Owned assets section
                // Get our owned assets from our view model
                // Display our owned assets
                if (ownedAssets.isNotEmpty()) {
                    items(ownedAssets) { asset ->
                        OwnedAssetItem(
                            asset,
                            onClickDelete = { asset -> ownedAssetsViewModel.deleteOwnedAsset(asset) }
                        )
                    }
                }

                // Update flag to only show pie chart, etc. when Update button is clicked

                // Update button
                item {
                    UpdateButton( onClickUpdate = {
                        timeSeriesViewModel.fetchTimeSeriesForAssets(ownedAssets)
                        isUpdateTriggered = true
                    })
                }

                if (isUpdateTriggered) {
                    // Create the pie chart composable when we have our finalized assetHoldingTotalValues map
                    // i.e. when the above block of code goes through
                    if (assetHoldingTotalValues.isNotEmpty()) {

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .background(Color.LightGray)
                            ) {
                                AssetsPieChart(assetHoldingTotalValues)
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorResource(id = R.color.pastel_blue))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Net Worth:",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$${netWorth ?: 0.0}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        assetHoldingTotalValues.forEach { (symbol, totalValue) ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = symbol,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "$${totalValue}",
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
                            }
                        }

                    } else {
                        item {
                            Text("Loading...", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItem(result: BestMatch, onAddAsset: (OwnedAsset) -> Unit) {
    var openDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }

    // Local function to avoid having to write duplicate code
    fun addAsset() {
        val quantityInt = quantity.toIntOrNull()
        if (quantityInt != null && quantityInt > 0) {
            val asset = OwnedAsset(
                symbol = result.symbol.toString(),
                name = result.name.toString(),
                type = result.type.toString(),
                region = result.region.toString(),
                quantity = quantityInt
            )
            onAddAsset(asset)
        }
    }

    Text(
        text = "${result.symbol} - ${result.name}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { openDialog = true }
        )

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            text = {
                Column {
                    Text(text = "Enter quantity")
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                quantity = newValue
                            }
                        },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            addAsset()
                            openDialog = false
                        })
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    addAsset()
                    openDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) { Text("Dismiss") }
            }
        )
    }
}

@Composable
fun OwnedAssetItem(asset: OwnedAsset, onClickDelete: (OwnedAsset) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete icon
        IconButton(
            onClick = { onClickDelete(asset) },
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete asset",
                tint = Color.Red
            )
        }
        Column {
            Row {
                Text(
                    text = asset.symbol,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " (${asset.quantity} shares)",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(text = "• ${asset.name}")
            Text(text = "• ${asset.type}")
            Text(text = "• ${asset.region}")
        }
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

@Composable
fun AssetsPieChart(assetHoldingTotalValues: Map<String, Double>) {
    // Generate a list of slices for the pie chart
    val pieChartData = PieChartData(
        slices = assetHoldingTotalValues.entries.map { entry ->
            PieChartData.Slice(
                label = entry.key,
                value = entry.value.toFloat(),
                // Generate a random colour based on the hash of the entry in the map
                color = generateColorFromHash(entry.key)
            )
        },
        plotType = PlotType.Pie
    )

    // Configure the pie chart
    val pieChartConfig = PieChartConfig(
        activeSliceAlpha = .9f,
        isEllipsizeEnabled = true,
        sliceLabelEllipsizeAt = TextUtils.TruncateAt.MIDDLE,
        isAnimationEnable = true,
        chartPadding = 20,
        showSliceLabels = true,
        labelVisible = true
    )

    PieChart(
        modifier = Modifier.fillMaxSize(),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig,
    )
}

fun generateColorFromHash(key: String): Color {
    val random = Random(key.hashCode())
    val red = random.nextInt(0, 256)
    val green = random.nextInt(0, 256)
    val blue = random.nextInt(0, 256)
    return Color(red, green, blue)
}

@Composable
fun BasicTableChart(data: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Data Rows
        data.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
        }
    }
}

package com.example.assetportfoliovisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assetportfoliovisualizer.ui.theme.AssetPortfolioVisualizerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {
    private val viewModel: TickerSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssetPortfolioVisualizerTheme {
                MyAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppScreen(viewModel: TickerSearchViewModel) {
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
                SectionTitle(stringResource(id = R.string.section_title_add_assets))
                TickerSearchField(viewModel)
                // Get the LiveData
                val searchResults by viewModel.searchResults.observeAsState(emptyList())

                LazyColumn {
                    items(searchResults) {
                        result -> SearchResultItem(result)
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
            viewModel.searchForSymbols(ticker)
        },
        label = { Text(stringResource(id = R.string.search_asset)) },
        placeholder = { Text(stringResource(id = R.string.search_asset_default)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

@Composable
fun SearchResultItem(result: BestMatch) {
    Text(
        text = "${result.symbol} - ${result.name}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

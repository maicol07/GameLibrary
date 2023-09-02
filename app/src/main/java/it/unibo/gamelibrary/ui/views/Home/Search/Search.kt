package it.unibo.gamelibrary.ui.views.Home.Search

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.common.Game.icon
import it.unibo.gamelibrary.ui.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.destinations.ProfileDestination
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.Genre
import ru.pixnews.igdbclient.model.Platform


private var isFiltersBottomSheetOpen by mutableStateOf(false)
private var currentSearch by mutableStateOf<SearchTypeObject<*, *>?>(null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    var active by rememberSaveable { mutableStateOf(false) }
    currentSearch = when (viewModel.searchType) {
        SearchType.GAMES -> viewModel.gamesSearch
        SearchType.USERS -> viewModel.usersSearch
    }
    androidx.compose.material3.SearchBar(
        query = currentSearch!!.query,
        onQueryChange = {
            currentSearch!!.query = it
            viewModel.search()
        },
        onSearch = { active = false },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search something...") },
        leadingIcon = {
            if (active) {
                IconButton(onClick = { active = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            if (active && viewModel.searchType !== SearchType.USERS) {
                IconButton(onClick = { isFiltersBottomSheetOpen = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        }
    ) {
        TabRow(selectedTabIndex = viewModel.searchType.ordinal) {
            for (searchType in SearchType.values()) {
                Tab(
                    text = { Text(searchType.text) },
                    selected = viewModel.searchType == searchType,
                    onClick = { viewModel.searchType = searchType }
                )
            }
        }
        if (currentSearch!!.inProgress) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (currentSearch!!.results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No results found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val state = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = state
                ) {
                    items(currentSearch!!.results, key = { when (it) {
                        is Game -> it.id
                        is User -> it.uid
                        else -> 0
                    } }) {
                        when (it) {
                            is Game -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            navController.navigate(
                                                GameViewNavDestination(it.id.toInt())
                                            )
                                        }
                                ) {
                                    GameCoverImage(
                                        game = it,
                                        modifier = Modifier.height(175.dp)
                                    )
                                    Text(
                                        text = it.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            is User -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            navController.navigate(
                                                ProfileDestination(
                                                    it.uid
                                                )
                                            )
                                        }
                                ) {
                                    val imageShape = RoundedCornerShape(128.dp)
                                    val imageModifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp)
                                        .clip(imageShape)
                                        .shadow(100.dp, imageShape)
                                    if (it.hasImage()) {
                                        CoilImage(
                                            imageModel = {
                                                Uri.parse(it.image)
                                            },
                                            imageOptions = ImageOptions(
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center
                                            ),
                                            modifier = imageModifier
                                        )
                                    } else {
                                        Icon(
                                            Icons.Outlined.AccountCircle,
                                            contentDescription = "User image",
                                            modifier = imageModifier
                                        )
                                    }
                                    val userText = if (it.name != null && it.surname != null) {
                                        "${it.name} ${it.surname}"
                                    } else {
                                        it.username
                                    }
                                    Text(
                                        text = userText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (isFiltersBottomSheetOpen) {
        FiltersBottomSheet()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersBottomSheet(viewModel: SearchViewModel = hiltViewModel()) {
    ModalBottomSheet(
        onDismissRequest = {
            isFiltersBottomSheetOpen = false
            viewModel.search()
        },
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Show DLCs", modifier = Modifier.padding(4.dp))
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentSearch!!.showDLCs,
                    onCheckedChange = {
                        currentSearch!!.showDLCs = it
                    },
                    modifier = Modifier.padding(4.dp)
                )
            }
            for (filterType in FilterType.entries) {
                val filter = currentSearch!!.filters[filterType]!!;
                Text(
                    text = filterType.text,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(4.dp)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (value in filter.values) {
                        val selected = filter.selected.contains(value)
                        FilterChip(
                            label = {
                                Text(
                                    text = when (value) {
                                        is Platform -> value.name
                                        is Genre -> value.name
                                        else -> value.toString()
                                    }
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (selected) {
                                    filter.selected.remove(value)
                                } else {
                                    filter.selected.add(value)
                                }
                            },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                } else {
                                    when (value) {
                                        is Platform -> {
                                            if (value.platform_logo !== null && value.platform_logo!!.id.toInt() != 0) {
                                                CoilImage(
                                                    imageModel = {
                                                        "https:${value.platform_logo!!.url}"
                                                    },
                                                    imageOptions = ImageOptions(
                                                        contentScale = ContentScale.FillBounds,
                                                        alignment = Alignment.Center
                                                    ),
                                                    modifier = Modifier.size(
                                                        FilterChipDefaults.IconSize
                                                    )
                                                )
                                            }
                                        }

                                        is Genre -> {
                                            val icon = value.icon
                                            if (icon is Int) {
                                                Icon(
                                                    painterResource(icon),
                                                    contentDescription = null
                                                )
                                            }
                                            if (icon is ImageVector) {
                                                Icon(
                                                    icon,
                                                    contentDescription = null
                                                )
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
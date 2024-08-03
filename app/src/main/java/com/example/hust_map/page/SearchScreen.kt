package com.example.hust_map.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amap.api.services.core.PoiItemV2

@Composable
fun ShowSearchScreen(
    list: ArrayList<PoiItemV2>,
    toShowMapScreen: () -> Unit,
    searchForPoi: (keyword: String) -> Unit,
    onSelected: (poiItem: PoiItemV2) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    if (keyword.isNotEmpty()) {
        searchForPoi(keyword)
    }
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(50.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { toShowMapScreen() })
                    TextField(value = keyword, onValueChange = { keyword = it })
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                searchForPoi(keyword)
                            })
                }
            }
            item { Spacer(modifier = Modifier.height(50.dp)) }
            if (list.isNotEmpty()) {
                items(list) { poiItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable {
                                onSelected(poiItem)
                                toShowMapScreen()
                            },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = poiItem.title,
                            style = TextStyle(fontSize = 20.sp),
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    DividerDefaults
                }
            }
        }
    }
}
package it.unibo.gamelibrary

import android.graphics.Paint.Align
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unibo.gamelibrary.ui.SharedTopAppBar
import it.unibo.gamelibrary.ui.SuperScaffold
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

class HomeActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GameLibraryTheme {
        Home()
    }
}

//placeholder, no idea on how to give the post for now
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post(id: Int) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,//TODO placeholder
                    contentDescription = "user profile image"
                )
                Text(text = "Username")//TODO placeholder
            }
            Image(
                imageVector = Icons.Filled.Photo,
                contentDescription = "post main image"
            )//TODO placeholder
            ReviewStars(3)//TODO placeholder
            Text(text = "post number: $id")//TODO placeholder
        }
    }
}

@Composable
fun ReviewStars(rating: Int) {
    Row() {
        var i: Int = 0;
        while (i < 5) {
            Icon(
                imageVector = if (i < rating) (Icons.Filled.Star) else (Icons.Outlined.Star),
                contentDescription = null
            )
            i++;
        }
    }
}



@Composable
fun HomeSection(title:String, itemList: List<String>) {
    Column() {//non so se serve la colum, vediamo come viene
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        LazyRow() {
            items(itemList) {
                //se c'è prendi game di nome stringa da api e crea card
            }
        }
    }
}

@Composable
fun Home() {
    SuperScaffold(title = "Home") {
        LazyColumn(
            contentPadding = it
        ) {
            items(count = 1) {
                Post(5)
            }
        }
    }
}




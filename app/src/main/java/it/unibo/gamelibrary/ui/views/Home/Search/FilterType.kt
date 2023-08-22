package it.unibo.gamelibrary.ui.views.Home.Search

enum class FilterType(val text: String, val apiField: String) {
    PLATFORMS("Platforms", "release_dates.platform"),
    GENRES("Genres", "genres"),
}
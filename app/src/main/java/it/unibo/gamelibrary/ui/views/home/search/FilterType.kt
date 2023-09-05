package it.unibo.gamelibrary.ui.views.home.search

enum class FilterType(val text: String, val apiField: String) {
    PLATFORMS("Platforms", "release_dates.platform"),
    GENRES("Genres", "genres"),
}
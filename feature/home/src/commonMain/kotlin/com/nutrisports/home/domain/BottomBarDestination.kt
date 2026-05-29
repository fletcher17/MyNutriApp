package com.nutrisports.home.domain

import com.nutrisport.shared.Resources
import com.nutrisport.shared.navigation.Screen
import org.jetbrains.compose.resources.DrawableResource

enum class BottomBarDestination (
    var icon: DrawableResource,
    var title: String,
    var screen: Screen
) {

    ProductOverview(
        icon = Resources.Icon.Home,
        title = "Nutri Sport",
        screen = Screen.ProductsOverview
    ),
    Cart(
    icon = Resources.Icon.ShoppingCart,
    title = "Cart",
    screen = Screen.Cart
    ),
    Category(
        icon = Resources.Icon.Categories,
        title = "Categories",
        screen = Screen.Category
    )
}
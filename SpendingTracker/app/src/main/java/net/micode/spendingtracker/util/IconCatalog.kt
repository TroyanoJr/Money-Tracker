package net.micode.spendingtracker.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconCatalog {
    data class IconSection(val name: String, val icons: List<Pair<String, ImageVector>>)

    val filledIconSections = listOf(
        IconSection("Bills", listOf(
            "FlashOn" to Icons.Default.FlashOn,
            "Lightbulb" to Icons.Default.Lightbulb,
            "LocalFireDepartment" to Icons.Default.LocalFireDepartment,
            "WaterDrop" to Icons.Default.WaterDrop,
            "Phone" to Icons.Default.Phone,
            "Subscriptions" to Icons.Default.Subscriptions,
            "Tv" to Icons.Default.Tv,
            "Router" to Icons.Default.Router,
            "Language" to Icons.Default.Language,
            "Wifi" to Icons.Default.Wifi,
            "CellTower" to Icons.Default.CellTower,
            "Umbrella" to Icons.Default.Umbrella,
            "ContentCut" to Icons.Default.ContentCut,
            "Apartment" to Icons.Default.Apartment
        )),
        IconSection("Clothes", listOf(
            "Checkroom" to Icons.Default.Checkroom,
            "DryCleaning" to Icons.Default.DryCleaning,
            "Iron" to Icons.Default.Iron,
            "Watch" to Icons.Default.Watch,
            "ShoppingBag" to Icons.Default.ShoppingBag,
            "Accessibility" to Icons.Default.Accessibility,
            "Face" to Icons.Default.Face
        )),
        IconSection("Wellbeing, Health, Beauty", listOf(
            "Person" to Icons.Default.Person,
            "People" to Icons.Default.People,
            "SelfImprovement" to Icons.Default.SelfImprovement,
            "Spa" to Icons.Default.Spa,
            "MedicalServices" to Icons.Default.MedicalServices,
            "HealthAndSafety" to Icons.Default.HealthAndSafety,
            "Vaccines" to Icons.Default.Vaccines,
            "Medication" to Icons.Default.Medication,
            "Sick" to Icons.Default.Sick,
            "Coronavirus" to Icons.Default.Coronavirus,
            "Dangerous" to Icons.Default.Dangerous,
            "SentimentSatisfied" to Icons.Default.SentimentSatisfied,
            "SentimentDissatisfied" to Icons.Default.SentimentDissatisfied,
            "Favorite" to Icons.Default.Favorite,
            "Star" to Icons.Default.Star,
            "CheckCircle" to Icons.Default.CheckCircle,
            "Brush" to Icons.Default.Brush,
            "ContentCut" to Icons.Default.ContentCut,
            "FaceRetouchingNatural" to Icons.Default.FaceRetouchingNatural,
            "KingBed" to Icons.Default.KingBed
        )),
        IconSection("Sports, Hobbies", listOf(
            "DirectionsWalk" to Icons.AutoMirrored.Filled.DirectionsWalk,
            "DirectionsRun" to Icons.AutoMirrored.Filled.DirectionsRun,
            "DirectionsBike" to Icons.AutoMirrored.Filled.DirectionsBike,
            "Pool" to Icons.Default.Pool,
            "DownhillSkiing" to Icons.Default.DownhillSkiing,
            "SportsSoccer" to Icons.Default.SportsSoccer,
            "SportsFootball" to Icons.Default.SportsFootball,
            "SportsBaseball" to Icons.Default.SportsBaseball,
            "SportsBasketball" to Icons.Default.SportsBasketball,
            "SportsTennis" to Icons.Default.SportsTennis,
            "SportsVolleyball" to Icons.Default.SportsVolleyball,
            "FitnessCenter" to Icons.Default.FitnessCenter,
            "GolfCourse" to Icons.Default.GolfCourse,
            "Hiking" to Icons.Default.Hiking,
            "CameraAlt" to Icons.Default.CameraAlt,
            "Movie" to Icons.Default.Movie,
            "Book" to Icons.Default.Book,
            "Extension" to Icons.Default.Extension,
            "Palette" to Icons.Default.Palette,
            "Piano" to Icons.Default.Piano
        )),
        IconSection("Household", listOf(
            "Home" to Icons.Default.Home,
            "Sell" to Icons.Default.Sell,
            "Assignment" to Icons.Default.Assignment,
            "ShoppingCart" to Icons.Default.ShoppingCart,
            "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
            "Work" to Icons.Default.Work,
            "LocalMall" to Icons.Default.LocalMall,
            "CardGiftcard" to Icons.Default.CardGiftcard,
            "ChildCare" to Icons.Default.ChildCare,
            "ChildFriendly" to Icons.Default.ChildFriendly,
            "FamilyRestroom" to Icons.Default.FamilyRestroom,
            "Construction" to Icons.Default.Construction,
            "Inventory" to Icons.Default.Inventory,
            "CleanHands" to Icons.Default.CleanHands,
            "Sanitizer" to Icons.Default.Sanitizer,
            "AirplanemodeActive" to Icons.Default.AirplanemodeActive,
            "AttachFile" to Icons.Default.AttachFile,
            "Email" to Icons.Default.Email,
            "Print" to Icons.Default.Print,
            "AlternateEmail" to Icons.Default.AlternateEmail,
            "Visibility" to Icons.Default.Visibility,
            "CleaningServices" to Icons.Default.CleaningServices
        )),
        IconSection("Food & Drink", listOf(
            "Restaurant" to Icons.Default.Restaurant,
            "LocalDrink" to Icons.Default.LocalDrink,
            "LocalPizza" to Icons.Default.LocalPizza,
            "BakeryDining" to Icons.Default.BakeryDining,
            "Cake" to Icons.Default.Cake,
            "SetMeal" to Icons.Default.SetMeal,
            "Icecream" to Icons.Default.Icecream,
            "LunchDining" to Icons.Default.LunchDining,
            "Kitchen" to Icons.Default.Kitchen,
            "LocalCafe" to Icons.Default.LocalCafe,
            "LocalBar" to Icons.Default.LocalBar,
            "WineBar" to Icons.Default.WineBar,
            "Liquor" to Icons.Default.Liquor,
            "Fastfood" to Icons.Default.Fastfood
        )),
        IconSection("Entertainment", listOf(
            "Smartphone" to Icons.Default.Smartphone,
            "Tablet" to Icons.Default.Tablet,
            "MusicNote" to Icons.Default.MusicNote,
            "DesktopWindows" to Icons.Default.DesktopWindows,
            "Laptop" to Icons.Default.Laptop,
            "Monitor" to Icons.Default.Monitor,
            "MusicVideo" to Icons.Default.MusicVideo,
            "Mic" to Icons.Default.Mic,
            "Album" to Icons.Default.Album,
            "LibraryMusic" to Icons.Default.LibraryMusic,
            "VolumeUp" to Icons.AutoMirrored.Filled.VolumeUp,
            "Speaker" to Icons.Default.Speaker,
            "ConfirmationNumber" to Icons.Default.ConfirmationNumber,
            "Casino" to Icons.Default.Casino,
            "SportsEsports" to Icons.Default.SportsEsports,
            "Gamepad" to Icons.Default.Gamepad,
            "VideogameAsset" to Icons.Default.VideogameAsset,
            "TheaterComedy" to Icons.Default.TheaterComedy
        )),
        IconSection("Travel", listOf(
            "DirectionsCar" to Icons.Default.DirectionsCar,
            "DirectionsBus" to Icons.Default.DirectionsBus,
            "DirectionsRailway" to Icons.Default.DirectionsRailway,
            "LocalTaxi" to Icons.Default.LocalTaxi,
            "TwoWheeler" to Icons.Default.TwoWheeler,
            "Sailing" to Icons.Default.Sailing,
            "Anchor" to Icons.Default.Anchor,
            "RocketLaunch" to Icons.Default.RocketLaunch,
            "LocalGasStation" to Icons.Default.LocalGasStation,
            "Speed" to Icons.Default.Speed,
            "LocalParking" to Icons.Default.LocalParking,
            "Toll" to Icons.Default.Toll,
            "RvHookup" to Icons.Default.RvHookup,
            "Terrain" to Icons.Default.Terrain,
            "Business" to Icons.Default.Business,
            "Luggage" to Icons.Default.Luggage,
            "Castle" to Icons.Default.Castle
        )),
        IconSection("Home, Nature, Pets", listOf(
            "VpnKey" to Icons.Default.VpnKey,
            "Key" to Icons.Default.Key,
            "Lock" to Icons.Default.Lock,
            "Chair" to Icons.Default.Chair,
            "TableBar" to Icons.Default.TableBar,
            "Bed" to Icons.Default.Bed,
            "Bathtub" to Icons.Default.Bathtub,
            "Handyman" to Icons.Default.Handyman,
            "Park" to Icons.Default.Park,
            "LocalFlorist" to Icons.Default.LocalFlorist,
            "Pets" to Icons.Default.Pets
        )),
        IconSection("Money", listOf(
            "Savings" to Icons.Default.Savings,
            "Atm" to Icons.Default.Atm,
            "Payments" to Icons.Default.Payments,
            "CreditCard" to Icons.Default.CreditCard,
            "AccountBalance" to Icons.Default.AccountBalance,
            "Calculate" to Icons.Default.Calculate,
            "Euro" to Icons.Default.Euro,
            "AttachMoney" to Icons.Default.AttachMoney,
            "CurrencyExchange" to Icons.Default.CurrencyExchange
        ))
    )

    fun getIconByName(name: String): ImageVector {
        return filledIconSections.flatMap { it.icons }.find { it.first == name }?.second ?: Icons.Default.Sell
    }
}

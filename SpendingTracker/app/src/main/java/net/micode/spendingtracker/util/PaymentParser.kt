package net.micode.spendingtracker.util

import java.util.regex.Pattern

object PaymentParser {
    // Keywords para identificar el tipo de transacción (Gasto/Ingreso)
    private val expenseKeywords = listOf(
        "支付成功", "付款", "支出", "消费", "支付", 
        "paid", "pago", "gastado", "spent", "purchase", "payment"
    )
    private val incomeKeywords = listOf(
        "收款", "到账", "收入", "退款", "红包",
        "received", "recibido", "ingreso", "refund", "deposit", "earned"
    )

    // Mapa de categorías estandarizadas (Global + China)
    private val categoryMap = mapOf(
        "Food & Dining" to listOf(
            "咖啡", "餐厅", "美食", "美团", "饿了么", "瑞幸", "麦当劳", "肯德基",
            "Coffee", "Luckin", "KFC", "McDonald", "Starbuck", "Food", "Restau", "Comida", "Cena", "Almuerzo"
        ),
        "Shopping" to listOf(
            "超市", "购物", "淘宝", "天猫", "京东", "拼多多", "闲鱼", "衣服", "鞋", "服装",
            "Mall", "Shop", "Walmart", "Amazon", "AliExpress", "Compra", "Tienda", "7-Eleven", "Lawson",
            "Zara", "Uniqlo", "H&M", "Clothes", "Ropa"
        ),
        "Transport" to listOf(
            "打车", "出行", "滴滴", "地铁", "公交", "铁路", "12306", "加油", "停车",
            "Didi", "Taxi", "Uber", "Cabify", "Viaje", "Gas", "Parking", "Train", "Metro", "Bus"
        ),
        "Utilities & Subs" to listOf(
            "宽带", "网络", "电信", "移动", "联通", "话费", "水费", "电费", "燃气",
            "Internet", "Fiber", "Netflix", "Spotify", "iCloud", "Disney", "Phone", "Water", "Electricity", "Fibra"
        ),
        "Health & Beauty" to listOf(
            "医药", "医院", "药店", "美容", "理发", "健身", 
            "Pharmacy", "Hospital", "Doctor", "Clinic", "Gym", "Skincare", "Makeup", "Salud", "Farmacia"
        ),
        "Entertainment" to listOf(
            "电影", "游戏", "充值", "酒吧", "KTV", 
            "Steam", "Nintendo", "PlayStation", "Movie", "Cinema", "Bar", "Club", "Show", "Fun"
        ),
        "Education" to listOf(
            "书", "课程", "学费", "培训", 
            "Books", "Course", "Tuition", "Udemy", "Coursera", "Escuela", "Libro"
        ),
        "Finance & Social" to listOf(
            "红包", "转账", "保险", "还款", "借款", "信用卡",
            "Transfer", "Insurance", "Refund", "Card", "Bill", "Loan", "Regalo", "Donación"
        )
    )

    data class ParseResult(
        val amount: Double,
        val isExpense: Boolean,
        val isIncome: Boolean,
        val category: String,
        val timestamp: Long
    )

    fun parse(content: String, postTime: Long = System.currentTimeMillis()): ParseResult? {
        // Regex mejorada para soportar más símbolos de moneda y formatos
        val amountPattern = Pattern.compile("(?:¥|\\$|€|£|HK\\$|金额|monto|total|支付|pago)\\s*([0-9,.]+)|([0-9,.]+)\\s*(?:元|usd|eur|gbp|pesos)?")
        val matcher = amountPattern.matcher(content)
        
        var amount: Double? = null
        if (matcher.find()) {
            val group1 = matcher.group(1)
            val group2 = matcher.group(2)
            val amountStr = (group1 ?: group2)?.replace(",", "")
            amount = amountStr?.toDoubleOrNull()
        }

        if (amount == null) return null

        val lowerContent = content.lowercase()
        val hasExpense = expenseKeywords.any { lowerContent.contains(it.lowercase()) }
        val hasIncome = incomeKeywords.any { lowerContent.contains(it.lowercase()) }

        // Si no detectamos ni gasto ni ingreso, por defecto tratamos de inferir por el contexto
        // pero para evitar falsos positivos en notificaciones aleatorias, requerimos keywords.
        if (!hasExpense && !hasIncome) return null
        
        val isExpense = hasExpense
        val isIncome = !hasExpense && hasIncome

        var detectedCategory = "General"
        // Buscamos categoría basándonos en keywords
        for ((category, keywords) in categoryMap) {
            if (keywords.any { lowerContent.contains(it.lowercase()) }) {
                detectedCategory = category
                break
            }
        }

        return ParseResult(amount, isExpense, isIncome, detectedCategory, postTime)
    }
}

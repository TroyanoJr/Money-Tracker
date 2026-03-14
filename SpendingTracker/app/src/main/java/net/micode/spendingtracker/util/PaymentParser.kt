package net.micode.spendingtracker.util

import java.util.regex.Pattern

object PaymentParser {
    // Patrones pre-compilados para máxima eficiencia y precisión decimal
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:¥|\\$|€|£|HK\\$|金额|monto|total|支付|pago|付款|amount)(?:[:：]|为)?\\s*([0-9,.]+)|([0-9,.]+)\\s*(?:元|usd|eur|gbp|pesos|hkd)?",
        Pattern.CASE_INSENSITIVE
    )

    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("在\\s*([^\\s,，。]+?)\\s*(?:消费|支付|付款)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:at|en|to)\\s*([^\\s,，。]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("“([^”]+)”")
    )

    // Keywords para identificar el tipo de transacción (Gasto/Ingreso)
    private val expenseKeywords = listOf(
        "支付成功", "付款", "支出", "消费", "支付", "账单", "付款成功", "成功付款",
        "paid", "pago", "gastado", "spent", "purchase", "payment", "alipay", "支付宝",
        "transaction", "withdrawal", "spent", "debit", "transfer out"
    )
    private val incomeKeywords = listOf(
        "收款", "到账", "收入", "退款", "红包", "收益",
        "received", "recibido", "ingreso", "refund", "deposit", "earned", "credit", "transfer in"
    )

    // Mapa de categorías estandarizadas
    private val categoryMap = mapOf(
        "Food & Dining" to listOf(
            "咖啡", "餐厅", "美食", "美团", "饿了么", "瑞幸", "麦当劳", "肯德基", "星巴克",
            "Coffee", "Luckin", "KFC", "McDonald", "Starbuck", "Food", "Restau", "Comida", "Cena", "Almuerzo"
        ),
        "Shopping" to listOf(
            "超市", "购物", "淘宝", "天猫", "京东", "拼多多", "闲鱼", "衣服", "鞋", "服装",
            "Mall", "Shop", "Walmart", "Amazon", "AliExpress", "Compra", "Tienda", "7-Eleven", "Lawson",
            "Zara", "Uniqlo", "H&M", "Clothes", "Ropa"
        ),
        "Transport" to listOf(
            "打车", "出行", "滴滴", "地铁", "公交", "铁路", "12306", "加油", "停车", "高德",
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
            "红包", "转账", "保险", "还款", "借款", "信用卡", "bank", "银行",
            "Transfer", "Insurance", "Refund", "Card", "Bill", "Loan", "Regalo", "Donación"
        )
    )

    data class ParseResult(
        val amount: Double,
        val isExpense: Boolean,
        val isIncome: Boolean,
        val category: String,
        val merchant: String?,
        val timestamp: Long,
        val isComplete: Boolean
    )

    fun parse(content: String, postTime: Long = System.currentTimeMillis()): ParseResult? {
        val lowerContent = content.lowercase()
        
        // 1. Extraer monto con precisión
        val matcher = AMOUNT_PATTERN.matcher(content)
        var amount: Double? = null
        
        while (matcher.find()) {
            val group1 = matcher.group(1)
            val group2 = matcher.group(2)
            val amountStr = (group1 ?: group2)?.replace(",", "")
            val parsed = amountStr?.toDoubleOrNull()
            
            if (parsed != null) {
                amount = parsed
                if (group1 != null) break 
            }
        }

        if (amount == null) return null

        // 2. Determinar dirección del flujo
        val hasExpense = expenseKeywords.any { lowerContent.contains(it) }
        val hasIncome = incomeKeywords.any { lowerContent.contains(it) }

        // Si no hay keywords pero detectamos un monto financiero claro, asumimos gasto por defecto
        if (!hasExpense && !hasIncome) return null
        
        val isExpense = hasExpense || (!hasIncome && lowerContent.contains("transaction"))
        val isIncome = !isExpense && hasIncome

        // 3. Extraer nombre del comercio
        var detectedMerchant: String? = null
        for (pattern in MERCHANT_PATTERNS) {
            val m = pattern.matcher(content)
            if (m.find()) {
                detectedMerchant = m.group(1)
                break
            }
        }

        // 4. Clasificación por categorías
        var detectedCategory: String? = null
        val searchContext = if (detectedMerchant != null) "$lowerContent ${detectedMerchant.lowercase()}" else lowerContent
        
        for ((category, keywords) in categoryMap) {
            if (keywords.any { searchContext.contains(it.lowercase()) }) {
                detectedCategory = category
                break
            }
        }

        val isComplete = detectedCategory != null
        val finalCategory = detectedCategory ?: "Pending"

        return ParseResult(amount, isExpense, isIncome, finalCategory, detectedMerchant, postTime, isComplete)
    }
}

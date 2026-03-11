package net.micode.spendingtracker.util

import java.util.regex.Pattern

object PaymentParser {
    // Añadimos keywords en español para tus pruebas manuales
    private val expenseKeywords = listOf("支付成功", "付款", "支出", "消费", "paid", "pago", "gastado", "spent")
    private val incomeKeywords = listOf("收款", "到账", "收入", "received", "recibido", "ingreso")

    private val categoryMap = mapOf(
        "Eating Out" to listOf("咖啡", "餐厅", "美食", "Coffee", "Luckin", "KFC", "McDonald", "饿了么", "Starbucks", "comida", "restaurante"),
        "Shopping" to listOf("超市", "购物", "淘宝", "天猫", "京东", "Mall", "Shop", "Walmart", "compra", "tienda"),
        "Travel" to listOf("打车", "出行", "滴滴", "地铁", "公交", "铁路", "12306", "Didi", "Taxi", "uber", "cabify", "viaje"),
        "Wifi" to listOf("宽带", "网络", "电信", "移动", "联通", "internet", "fibra"),
        "Clothes" to listOf("衣服", "鞋", "服装", "Zara", "Uniqlo", "H&M", "ropa")
    )

    data class ParseResult(
        val amount: Double,
        val isExpense: Boolean,
        val isIncome: Boolean,
        val category: String,
        val timestamp: Long
    )

    fun parse(content: String, postTime: Long = System.currentTimeMillis()): ParseResult? {
        // Regex ultra-flexible: Detecta ¥, $, o palabras como "monto", "total" seguidas de números
        // También detecta números sueltos con decimales al final de la frase
        val amountPattern = Pattern.compile("(?:¥|\\$|金额|monto|total|支付|pago)\\s*([0-9,.]+)|([0-9,.]+)\\s*(?:元|元|usd|eur|pesos)?")
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
        val hasExpense = expenseKeywords.any { lowerContent.contains(it) }
        val hasIncome = incomeKeywords.any { lowerContent.contains(it) }

        if (!hasExpense && !hasIncome) return null
        
        val isExpense = hasExpense
        val isIncome = !hasExpense && hasIncome

        var detectedCategory = "General"
        if (isExpense) {
            for ((category, keywords) in categoryMap) {
                if (keywords.any { lowerContent.contains(it) }) {
                    detectedCategory = category
                    break
                }
            }
        }

        return ParseResult(amount, isExpense, isIncome, detectedCategory, postTime)
    }
}

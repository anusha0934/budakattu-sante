package com.mindmatrix.budakattusante.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.mindmatrix.budakattusante.data.model.SupplyLog
import java.io.File

object ExportUtils {
    fun exportSupplyLogToCsv(context: Context, logs: List<SupplyLog>) {
        val header = "BatchID,Product,Artisan,Quantity,HarvestDate\n"
        val csvData = header + logs.joinToString("\n") { 
            "${it.batchId},${it.productName},${it.familyName},${it.totalQuantityKg},${it.harvestDate}"
        }

        val file = File(context.cacheDir, "supply_report.csv")
        file.writeText(csvData)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Report"))
    }
}

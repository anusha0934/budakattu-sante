package com.mindmatrix.budakattusante.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.mindmatrix.budakattusante.data.local.BudakattuDatabase
import com.mindmatrix.budakattusante.data.local.entity.MspEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MSPRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = BudakattuDatabase.getInstance(context)
    private val mspDao = db.mspDao()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("msp_data").get().await()
            val mspList = snapshot.documents.mapNotNull { doc ->
                val category = doc.id
                val approvedMsp = doc.getDouble("approvedMsp") ?: 0.0
                val marketPrice = doc.getDouble("marketPrice") ?: 0.0
                val lastUpdated = doc.getString("lastUpdated") ?: ""
                val authority = doc.getString("authority") ?: "TRIFED"
                
                MspEntity(
                    category = category,
                    approvedMsp = approvedMsp,
                    marketPrice = marketPrice,
                    lastUpdated = lastUpdated,
                    authority = authority
                )
            }

            if (mspList.isNotEmpty()) {
                mspDao.insertMspData(mspList)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("MSPRefreshWorker", "Failed to refresh MSP data", e)
            Result.retry()
        }
    }
}

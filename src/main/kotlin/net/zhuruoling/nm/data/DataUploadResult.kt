package net.zhuruoling.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class DataUploadResult(val result: Result, val message: String = ""){
    enum class Result{
        SUCCESS,FAILURE
    }
}

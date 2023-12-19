package net.zhuruoling.nm.data

data class DataUploadResult(val result: Result, val message: String = ""){
    enum class Result{
        SUCCESS,FAILURE
    }
}

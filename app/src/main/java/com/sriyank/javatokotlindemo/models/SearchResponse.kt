package com.sriyank.javatokotlindemo.models

import io.realm.RealmObject
import com.google.gson.annotations.SerializedName
import io.realm.RealmList

//class SearchResponse : RealmObject() {
//    @SerializedName("total_count")
//    var totalCount = 0
//    var items: RealmList<Repository>? = null
//}

data class SearchResponse (@SerializedName("total_count")
                      var totalCount: Int, var items: RealmList<Repository>?)
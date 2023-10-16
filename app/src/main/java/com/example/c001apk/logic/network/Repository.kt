package com.example.c001apk.logic.network

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object Repository {

    fun getHomeFeed(page: Int, firstLaunch: Int, installTime: String, lastItem: String) =
        fire(Dispatchers.IO) {
            val homeFeedResponse = Network.getHomeFeed(page, firstLaunch, installTime, lastItem)
            if (homeFeedResponse.data.isNotEmpty())
                Result.success(homeFeedResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getFeedContent(id: String) = fire(Dispatchers.IO) {
        val feedResponse = Network.getFeedContent(id)
        if (feedResponse.data != null)
            Result.success(feedResponse)
        else
            Result.failure(RuntimeException("response status is null"))
    }

    fun getFeedContentReply(id: String, discussMode: Int, listType: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedReplyResponse = Network.getFeedContentReply(id, discussMode, listType, page)
            if (feedReplyResponse.data.isNotEmpty())
                Result.success(feedReplyResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getSearchFeed(type: String, feedType: String, sort: String, keyWord: String, page: Int) =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getSearchFeed(type, feedType, sort, keyWord, page)
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getSearchUser(keyWord: String, page: Int) =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getSearchUser(keyWord, page)
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getReply2Reply(id: String, page: Int) =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getReply2Reply(id, page)
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getTopicLayout(tag: String) =
        fire(Dispatchers.IO) {
            val topicLayoutResponse = Network.getTopicLayout(tag)
            if (topicLayoutResponse.data != null)
                Result.success(topicLayoutResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getTopicData(url: String, title: String, subTitle: String?, page: Int) =
        fire(Dispatchers.IO) {
            val topicDataResponse = Network.getTopicData(url, title, subTitle, page)
            if (topicDataResponse.data.isNotEmpty())
                Result.success(topicDataResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

}
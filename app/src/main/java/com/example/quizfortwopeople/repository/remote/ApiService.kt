package com.example.quizfortwopeople.repository.remote

import com.example.quizfortwopeople.constants.AppConstant
import com.example.quizfortwopeople.model.responsepojo.QuestionsModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(AppConstant.API)
    suspend fun getQuestions(
        @Query("amount") amount: Int = 5
    ): Response<QuestionsModel>
}
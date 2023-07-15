package com.example.quizfortwopeople.model.responsepojo


import com.google.gson.annotations.SerializedName

data class QuestionsModel(
    @SerializedName("response_code")
    val responseCode: Int,
    @SerializedName("results")
    val results: MutableList<Result>
) {
    data class Result(
        @SerializedName("category")
        var category: String,
        @SerializedName("correct_answer")
        val correctAnswer: String,
        @SerializedName("difficulty")
        val difficulty: String,
        @SerializedName("incorrect_answers")
        val incorrectAnswers: List<String>,
        @SerializedName("question")
        val question: String,
        @SerializedName("type")
        val type: String
    )
}
package com.example.quizfortwopeople.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
class QuestionsModelDb(
    val category: String,
    val type: String,
    val difficulty: String,
    @PrimaryKey
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>,
    val answerByA: String,
    val answerByB: String,
    val attemptedQuestion: Boolean,
)
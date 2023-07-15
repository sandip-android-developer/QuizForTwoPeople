package com.example.quizfortwopeople.repository.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.quizfortwopeople.model.local.QuestionsModelDb


@Dao
interface QuizDao {
    @Insert
    suspend fun insertQuestions(questionsModelDb: QuestionsModelDb)

    @Query("SELECT * FROM questions")
    fun getAllQuestions(): LiveData<List<QuestionsModelDb>>

    @Query("UPDATE questions SET answerByA=:ans WHERE question=:quest")
    suspend fun updateAnswerForUserA(ans: String, quest: String)

    @Query("UPDATE questions SET answerByB=:ans WHERE question=:quest")
    suspend fun updateAnswerForUserB(ans: String, quest: String)

    @Query("UPDATE questions SET attemptedQuestion=:attempted WHERE question=:quest")
    suspend fun updateAttemptedStatus(attempted: Boolean, quest: String)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    @Query("SELECT * FROM questions WHERE attemptedQuestion=:attempted")
    fun getAllAttemptedQuestions(attempted: Boolean): LiveData<List<QuestionsModelDb>>
}
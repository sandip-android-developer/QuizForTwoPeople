package com.example.quizfortwopeople.repository.remote

import androidx.lifecycle.LiveData
import com.example.quizfortwopeople.model.local.QuestionsModelDb
import com.example.quizfortwopeople.repository.local.QuizDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val apiService: ApiService,
    private val quizDao: QuizDao
) {
    suspend fun getAllQuestions() =
        apiService.getQuestions()

    suspend fun insertQuestions(questionsModelDb: QuestionsModelDb) {
        quizDao.insertQuestions(questionsModelDb)
    }

    fun observerQuestions(): LiveData<List<QuestionsModelDb>> {
        return quizDao.getAllQuestions()
    }

    suspend fun deleteAllQuestions() {
        return quizDao.deleteAllQuestions()
    }

    suspend fun updateAnswerForUserA(ans: String, question:String){
        quizDao.updateAnswerForUserA(ans, question)
    }

    suspend fun updateAnswerForUserB(ans: String, question:String){
        quizDao.updateAnswerForUserB(ans, question)
    }

    suspend fun updateAttemptedStatus(attempted: Boolean, question:String){
        quizDao.updateAttemptedStatus(attempted, question)
    }

    fun getAllAttemptedQuestions(): LiveData<List<QuestionsModelDb>>{
        return quizDao.getAllAttemptedQuestions(true)
    }

}
package com.example.quizfortwopeople.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizfortwopeople.QuizApplication
import com.example.quizfortwopeople.model.local.QuestionsModelDb
import com.example.quizfortwopeople.model.responsepojo.QuestionsModel
import com.example.quizfortwopeople.repository.remote.MainRepository
import com.example.quizfortwopeople.utils.CommonUtils
import com.example.quizfortwopeople.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    private val _questionsListFromLocal =
        MutableLiveData<Resource<MutableList<QuestionsModel.Result>>>()
    val questionsListFromLocal: LiveData<Resource<MutableList<QuestionsModel.Result>>>
        get() = _questionsListFromLocal

    private val _questionsListFromServer =
        MutableLiveData<Resource<MutableList<QuestionsModel.Result>>>()
    val questionsListFromServer: LiveData<Resource<MutableList<QuestionsModel.Result>>>
        get() = _questionsListFromServer

    init {
        if (CommonUtils.isConnected(QuizApplication.applicationContext())) {
            getQuestionsFromServer()
        } else {
            getQuestionsFromLocal()
        }
    }

    fun getQuestionsFromServer(isNewQuiz: Boolean = true) = viewModelScope.launch {
        _questionsListFromServer.postValue(Resource.loading(null))
        if (isNewQuiz) {
            repository.deleteAllQuestions()
        }

        repository.getAllQuestions().let {
            if (it.isSuccessful) {
                if (it.body() != null) {
                    it.body()!!.results.forEach { result ->
                        repository.insertQuestions(
                            QuestionsModelDb(
                                category = result.category ?: "",
                                type = result.type ?: "",
                                difficulty = result.difficulty ?: "",
                                question = result.question ?: "",
                                correct_answer = result.correctAnswer ?: "",
                                incorrect_answers = result.incorrectAnswers,
                                answerByA = "NA",
                                answerByB = "NA",
                                attemptedQuestion = false
                            )
                        )
                    }
                    _questionsListFromServer.postValue(Resource.success(it.body()!!.results))
                } else {
                    _questionsListFromServer.postValue(
                        Resource.error(
                            it.errorBody().toString(),
                            null
                        )
                    )
                }
            } else {
                _questionsListFromServer.postValue(Resource.error(it.errorBody().toString(), null))
            }
        }
    }

    fun getQuestionsFromLocal() = viewModelScope.launch {
        _questionsListFromLocal.postValue(Resource.loading(null))
        val questionList: MutableList<QuestionsModel.Result> =
            mutableListOf()
        repository.observerQuestions().observeForever(Observer { it ->
            it?.forEach { questions ->

                val question: QuestionsModel.Result =
                    QuestionsModel.Result(
                        category = questions.category,
                        type = questions.type,
                        difficulty = questions.difficulty,
                        question = questions.question,
                        correctAnswer = questions.correct_answer,
                        incorrectAnswers = questions.incorrect_answers
                    )
                questionList.add(question)
            }
            _questionsListFromLocal.postValue(Resource.success(questionList))
        })

    }

    fun updateAnswerForA(ans: String, question: String) {
        viewModelScope.launch {
            repository.updateAnswerForUserA(ans, question)
        }
    }

    fun updateAnswerForB(ans: String, question: String) {
        viewModelScope.launch {
            repository.updateAnswerForUserB(ans, question)
        }
    }

    fun updateAttemptedStatus(attempted: Boolean, question: String) {
        viewModelScope.launch {
            repository.updateAttemptedStatus(attempted, question)
        }
    }

    fun getAllAttemptedQuestions(): LiveData<List<QuestionsModelDb>> {
        return repository.getAllAttemptedQuestions()
    }
}
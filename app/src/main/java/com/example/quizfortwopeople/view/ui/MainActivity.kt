package com.example.quizfortwopeople.view.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.quizfortwopeople.R
import com.example.quizfortwopeople.databinding.ActivityMainBinding
import com.example.quizfortwopeople.model.local.QuestionsModelDb
import com.example.quizfortwopeople.model.responsepojo.QuestionsModel
import com.example.quizfortwopeople.utils.CommonUtils
import com.example.quizfortwopeople.utils.Coroutine.observeOnce
import com.example.quizfortwopeople.utils.Status
import com.example.quizfortwopeople.view.base.BaseActivity
import com.example.quizfortwopeople.viewmodels.QuizViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Random


@AndroidEntryPoint
class MainActivity : BaseActivity(), View.OnClickListener {
    var scoreA = 0
    var scoreB = 0
    private var totalQuestion: Int = 0
    var currentQuestionIndex = 0
    var selectedAnswer = ""
    private val questionList: MutableList<QuestionsModel.Result> = mutableListOf()
    private val viewModel: QuizViewModel by viewModels()
    private val TAG: String = "MainActivity"
    private var file_name_path_a = "result_a.pdf"
    private var file_name_path_b = "result_b.pdf"
    private var PERMISSION_ALL = 1
    private var isResultNotTie: Boolean = true

    private var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private val bindingMainActivity by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private fun questionListApiObserver() {
        if (CommonUtils.isConnected(this)) {
            viewModel.questionsListFromServer.observe(this, Observer { it ->
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            hideProgress()
                            resource.data?.let { questions ->
                                totalQuestion = questions.size
                                questionList.addAll(questions)
                                if (questions.size > 0) {
                                    isResultNotTie = questionList.size == currentQuestionIndex
                                    if (currentQuestionIndex >= totalQuestion) {
                                        bindingMainActivity.txtTotalQuestion.text =
                                            "Total question is : ${currentQuestionIndex}"
                                    } else {
                                        bindingMainActivity.txtTotalQuestion.text =
                                            "Total question is : ${questions.size}"
                                    }
                                    loadNewQuestion(questionList)
                                } else {
                                    Snackbar.make(
                                        bindingMainActivity.root,
                                        "Something went wrong",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.ERROR -> {
                            it.message?.let { it1 ->
                                Snackbar.make(
                                    bindingMainActivity.root,
                                    it1,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            hideProgress()
                        }

                        Status.LOADING -> {
                            Log.i(TAG, "Server Data loading...")
                            showProgress(this)
                        }
                    }

                }
            })
        } else {
            viewModel.questionsListFromLocal.observeOnce(this, Observer { it ->
                Log.e(
                    TAG,
                    "Result is tie:" + isResultNotTie + ",totalQuestion::" + (totalQuestion != currentQuestionIndex)
                )
                if (isResultNotTie) {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                resource.data?.let { questions ->
                                    totalQuestion = questions.size
                                    bindingMainActivity.txtTotalQuestion.text =
                                        "Total question is : ${questions.size}"
                                    questionList.addAll(questions)
                                    isResultNotTie = totalQuestion == questionList.size
                                    loadNewQuestion(questions)
                                }
                            }

                            Status.ERROR -> {
                                it.message?.let { it1 ->
                                    Snackbar.make(
                                        bindingMainActivity.root,
                                        it1,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            Status.LOADING -> {
                                Log.i(TAG, "Local Data loading...")
                            }
                        }

                    }
                }
            })
        }

    }

    private fun initView() {
        bindingMainActivity.ansA.setOnClickListener(this)
        bindingMainActivity.ansB.setOnClickListener(this)
        bindingMainActivity.ansC.setOnClickListener(this)
        bindingMainActivity.ansD.setOnClickListener(this)
        bindingMainActivity.btnResults.setOnClickListener(this)

        bindingMainActivity.btnResults.visibility = View.GONE
        bindingMainActivity.group.visibility = View.VISIBLE

        questionListApiObserver()

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        if (!hasPermissions(this@MainActivity, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, PERMISSION_ALL)
        }
    }

    override fun onClick(view: View) {

        bindingMainActivity.ansA.setBackgroundColor(Color.WHITE)
        bindingMainActivity.ansB.setBackgroundColor(Color.WHITE)
        bindingMainActivity.ansC.setBackgroundColor(Color.WHITE)
        bindingMainActivity.ansD.setBackgroundColor(Color.WHITE)

        val clickedButton = view as Button

        if (clickedButton.id == R.id.btnResults) {
            finishQuiz()
        } else {
            //choices button clicked
            selectedAnswer = clickedButton.text.toString()
            clickedButton.setBackgroundColor(Color.MAGENTA)
        }
    }

    private fun loadNewQuestion(data: MutableList<QuestionsModel.Result>) {
        bindingMainActivity.person2.progress = 0
        Log.e(
            TAG,
            "loadNewQuestion::currentQuestionIndex::" + currentQuestionIndex + ",isResultTie::" + isResultNotTie + ",data.size:" + Gson().toJson(
                questionList
            )
        )
        if (currentQuestionIndex == questionList.size || isResultNotTie) {
            bindingMainActivity.btnResults.visibility = View.VISIBLE
            bindingMainActivity.group.visibility = View.GONE
            if (isResultNotTie) {
                isResultNotTie = false
            }
            return
        }
        bindingMainActivity.txtQuestion.text = data[currentQuestionIndex].question

        if (data[currentQuestionIndex].type == "boolean") {
            bindingMainActivity.ansC.visibility = View.GONE
            bindingMainActivity.ansD.visibility = View.GONE
            val options: MutableList<String> = ArrayList()
            options.add(data[currentQuestionIndex].correctAnswer)
            options.add(data[currentQuestionIndex].incorrectAnswers.get(0))
            options.shuffle(Random())
            bindingMainActivity.ansA.text = options[0]
            bindingMainActivity.ansB.text = options[1]

        } else if (data[currentQuestionIndex].type == "multiple") {
            bindingMainActivity.ansC.visibility = View.VISIBLE
            bindingMainActivity.ansD.visibility = View.VISIBLE

            val options: MutableList<String> = ArrayList()
            options.add(data[currentQuestionIndex].correctAnswer)
            options.add(data[currentQuestionIndex].incorrectAnswers[0])
            options.add(data[currentQuestionIndex].incorrectAnswers[1])
            options.add(data[currentQuestionIndex].incorrectAnswers[2])
            options.shuffle(Random())
            bindingMainActivity.ansA.text = options[0]
            bindingMainActivity.ansB.text = options[1]
            bindingMainActivity.ansC.text = options[2]
            bindingMainActivity.ansD.text = options[3]
        }
        timerForPersonA.start()
    }

    private fun finishQuiz() {
        var passStatus = ""
        if (scoreA > scoreB) {
            isResultNotTie = true
            passStatus = "Hurry!! Person A won the quiz"
        } else if (scoreA == scoreB) {
            passStatus = "Oops!! Quiz has been tied"
            isResultNotTie = false
        } else {
            isResultNotTie = true
            passStatus = "Hurry!! Person B won the quiz"
        }

        if (isResultNotTie) {

            AlertDialog.Builder(this)
                .setTitle(passStatus)
                .setMessage("Person A Score is $scoreA out of $totalQuestion \nPerson B Score is $scoreB out of $totalQuestion")
                .setPositiveButton(
                    "End Quiz"
                ) { dialogInterface: DialogInterface?, i: Int -> endQuiz() }
                .setCancelable(false)
                .show()
        } else {
            val questionAttempted = if (currentQuestionIndex == questionList.size) {
                questionList.size
            } else {
                1
            }
            AlertDialog.Builder(this)
                .setTitle(passStatus)
                .setMessage("Person A Score is $scoreA out of $questionAttempted \nPerson B Score is $scoreB out of $questionAttempted")
                .setPositiveButton(
                    "Tie"
                ) { dialogInterface: DialogInterface?, i: Int ->
                    scoreA = 0
                    scoreB = 0
                    bindingMainActivity.btnResults.visibility = View.GONE
                    bindingMainActivity.group.visibility = View.VISIBLE
                    bindingMainActivity.txtTotalQuestion.text =
                        "Total question is : ${currentQuestionIndex + 1}"

                    if (questionAttempted == 1) {
                        loadNewQuestion(questionList)
                    } else {
                        viewModel.getQuestionsFromServer(isNewQuiz = false)
                    }
                }
                .setCancelable(false)
                .show()

        }
    }

    private fun endQuiz() {
        scoreA = 0
        scoreB = 0
        currentQuestionIndex = 0
        bindingMainActivity.btnResults.visibility = View.GONE
        bindingMainActivity.group.visibility = View.VISIBLE
        Toast.makeText(
            this,
            "You can check you all attempted answer in downloaded folder",
            Toast.LENGTH_LONG
        ).show()

        lifecycleScope.launch() {
            launch { createPdf(file_name_path_a, "A") }
            launch { createPdf(file_name_path_b, "B") }
        }
    }

    private val timerForPersonA = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            bindingMainActivity.person1.progress = (100 - (millisUntilFinished / 300).toInt())
        }

        override fun onFinish() {
            bindingMainActivity.person1.progress = 100
            if (selectedAnswer.isNullOrEmpty()) {
                selectedAnswer = "NA"
            }
            viewModel.updateAnswerForA(selectedAnswer, questionList[currentQuestionIndex].question)
            if (selectedAnswer == questionList[currentQuestionIndex].correctAnswer) {
                scoreA++
            }
            selectedAnswer = ""
            timerForPersonB.start()
            bindingMainActivity.ansA.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansB.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansC.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansD.setBackgroundColor(Color.WHITE)
        }
    }
    private val timerForPersonB = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            bindingMainActivity.person2.progress = (100 - (millisUntilFinished / 300).toInt())
        }

        override fun onFinish() {
            bindingMainActivity.person2.progress = 100
            if (selectedAnswer.isNullOrEmpty()) {
                selectedAnswer = "NA"
            }
            viewModel.updateAnswerForB(selectedAnswer, questionList[currentQuestionIndex].question)
            viewModel.updateAttemptedStatus(true, questionList[currentQuestionIndex].question)
            if (selectedAnswer == questionList[currentQuestionIndex].correctAnswer) {
                scoreB++
            }
            currentQuestionIndex++
            selectedAnswer = ""
            bindingMainActivity.ansA.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansB.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansC.setBackgroundColor(Color.WHITE)
            bindingMainActivity.ansD.setBackgroundColor(Color.WHITE)
            if (currentQuestionIndex >= totalQuestion) {
                isResultNotTie = true
            }
            loadNewQuestion(questionList)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton(
                "Yes"
            ) { dialogInterface: DialogInterface?, i: Int -> finish() }
            .setNegativeButton("No") { dialogInterface: DialogInterface?, i: Int ->
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerForPersonA.cancel()
        timerForPersonB.cancel()
    }

    private suspend fun createPdf(fileName: String, user: String) {
        val pageWidth = 300f
        val pageHeight = 470f
        val myPdfDocument = PdfDocument()

        viewModel.getAllAttemptedQuestions()
            .observeOnce(this, Observer<List<QuestionsModelDb>> { localQuestion ->
                localQuestion.forEach {
                    val myPageInfo =
                        PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1)
                            .create()
                    val documentPage = myPdfDocument.startPage(myPageInfo)
                    val canvas: Canvas = documentPage.canvas
                    var y = 50f
                    var x = 20f

                    val textPaint = TextPaint()
                    textPaint.textSize = 16f
                    val mTextLayout = StaticLayout(
                        it.question,
                        textPaint,
                        canvas.width - 50,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false
                    )
                    y += textPaint.descent() - textPaint.ascent() - 10
                    canvas.save()
                    canvas.translate(x, y)
                    mTextLayout.draw(canvas)
                    x = 5f

                    val mTextLayout1 = StaticLayout(
                        it.correct_answer,
                        textPaint,
                        canvas.width - 50,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false
                    )

                    canvas.save()
                    x = 20f
                    y += textPaint.descent() - textPaint.ascent();
                    canvas.translate(x, y)
                    mTextLayout1.draw(canvas)

                    it.incorrect_answers.forEach { answer ->
                        val mTextLayout2 = StaticLayout(
                            answer,
                            textPaint,
                            canvas.width - 50,
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0.0f,
                            false
                        )

                        canvas.save()
                        x = 0f
                        y -= 10
                        canvas.translate(x, y)
                        mTextLayout2.draw(canvas)
                    }
                    var correct = ""
                    correct = if (user == "A") {
                        "Correct answer is : " + it.answerByA
                    } else {
                        "Correct answer is : " + it.answerByB
                    }
                    val mTextLayout4 = StaticLayout(
                        correct,
                        textPaint,
                        canvas.width - 50,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false
                    )

                    canvas.save()
                    y += textPaint.descent() - textPaint.ascent() - 24;
                    textPaint.color = Color.GREEN
                    canvas.translate(x, y)
                    mTextLayout4.draw(canvas)
                    myPdfDocument.finishPage(documentPage)
                }
            })


        val downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val file = File(downloadPath, fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            val fos = FileOutputStream(file)
            myPdfDocument.writeTo(fos)
            myPdfDocument.close()
            fos.close()
        } catch (fnp: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException() :$fnp")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "IOException :$e")
        }

    }

    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}
package com.example.quizfortwopeople.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quizfortwopeople.model.local.QuestionsModelDb

@Database(entities = [QuestionsModelDb::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class QuizDataBase : RoomDatabase() {
    abstract fun getQuizDao(): QuizDao
}
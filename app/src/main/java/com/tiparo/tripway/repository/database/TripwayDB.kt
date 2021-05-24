package com.tiparo.tripway.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tiparo.tripway.models.Point
import com.tiparo.tripway.models.Trip

@Database(entities = [Point::class, Trip::class], version = 1)
@TypeConverters(Converter::class)
abstract class TripwayDB : RoomDatabase() {

    abstract fun tripDao(): TripDao

    abstract fun pointDao(): PointDao
}
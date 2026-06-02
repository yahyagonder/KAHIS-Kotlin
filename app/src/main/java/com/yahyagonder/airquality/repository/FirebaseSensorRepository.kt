package com.yahyagonder.airquality.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yahyagonder.airquality.data.SensorData
import com.yahyagonder.airquality.data.SharedLocationData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseSensorRepository {
    private val database = FirebaseDatabase.getInstance("https://airquality-54999-default-rtdb.europe-west1.firebasedatabase.app")
    private val sensorDataRef = database.getReference("sensor_data")
    private val sharedLocationsRef = database.getReference("shared_locations")


    fun getSensorDataFlow(): Flow<SensorData> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                if (data != null) {
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        sensorDataRef.addValueEventListener(listener)

        awaitClose {
            sensorDataRef.removeEventListener(listener)
        }
    }

    fun pushSharedLocation(data: SharedLocationData) {
        val newRef = sharedLocationsRef.push()
        val dataWithId = data.copy(id = newRef.key ?: "")
        newRef.setValue(dataWithId)
    }

    fun deleteSharedLocation(id: String) {
        sharedLocationsRef.child(id).removeValue()
    }

    fun getSharedLocationsFlow(): Flow<List<SharedLocationData>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SharedLocationData>()
                for (child in snapshot.children) {
                    val location = child.getValue(SharedLocationData::class.java)
                    if (location != null) {
                        list.add(location)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        sharedLocationsRef.addValueEventListener(listener)

        awaitClose {
            sharedLocationsRef.removeEventListener(listener)
        }
    }
}

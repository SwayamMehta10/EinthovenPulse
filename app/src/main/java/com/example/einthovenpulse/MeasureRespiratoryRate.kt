package com.example.einthovenpulse

import kotlin.math.abs
import kotlin.math.pow
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.*

class MeasureRespiratoryRate(context: Context): SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val accelValuesX = mutableListOf<Float>()
    private val accelValuesY = mutableListOf<Float>()
    private val accelValuesZ = mutableListOf<Float>()

    private var isMeasuring = false
    private var job: Job? = null

    fun startMeasuring() {
        if (accelerometer != null && !isMeasuring) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            isMeasuring = true
            accelValuesX.clear()
            accelValuesY.clear()
            accelValuesZ.clear()

            job = CoroutineScope(Dispatchers.Default).launch {
                delay(45000L) // 45 seconds delay
                stopMeasuring()
            }
        }
    }

    fun stopMeasuring() {
        sensorManager.unregisterListener(this)
        isMeasuring = false
        job?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelValuesX.add(it.values[0])
                accelValuesY.add(it.values[1])
                accelValuesZ.add(it.values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    fun calculateRespiratoryRate(): Int {
        return respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)
    }

    private fun respiratoryRateCalculator(
        accelValuesX: MutableList<Float>,
        accelValuesY: MutableList<Float>,
        accelValuesZ: MutableList<Float>,
    ): Int {
        var previousValue: Float
        var currentValue: Float
        previousValue = 10f
        var k = 0
        for (i in 11..<accelValuesY.size) {
            currentValue = kotlin.math.sqrt(
                accelValuesZ[i].toDouble().pow(2.0) + accelValuesX[i].toDouble()
                    .pow(2.0) + accelValuesY[i].toDouble().pow(2.0)
            ).toFloat()
            if (abs(x = previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }
        val ret = (k.toDouble() / 45.00)
        return (ret * 30).toInt()
    }
}
package com.example.lr5v

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

	private lateinit var керуванняСенсором: SensorManager
	private var акселерометр: Sensor? = null

	private lateinit var статусСенсора: TextView
	private lateinit var полеX: TextView
	private lateinit var полеY: TextView
	private lateinit var полеZ: TextView
	private lateinit var полеПрискорення: TextView
	private lateinit var полеGСила: TextView
	private lateinit var полеПік: TextView
	private lateinit var полеРух: TextView

	private lateinit var прогресGСила: ProgressBar

	private var максимумGСила = 0.0f

    companion object {
        private const val ЗЕМНЕ_ПРИСКОРЕННЯ = 9.81f
        private const val КЛЮЧ_МАКС_G = "ключ_макс_g"
        private const val КЛЮЧ_СТАТУС = "ключ_статус"
        private const val КЛЮЧ_X = "ключ_x"
        private const val КЛЮЧ_Y = "ключ_y"
        private const val КЛЮЧ_Z = "ключ_z"
        private const val КЛЮЧ_ПРИСКОРЕННЯ = "ключ_прискорення"
        private const val КЛЮЧ_GСИЛА = "ключ_gсила"
        private const val КЛЮЧ_ПІК = "ключ_пік"
        private const val КЛЮЧ_РУХ = "ключ_рух"
        private const val КЛЮЧ_ПРОГРЕС = "ключ_прогрес"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ініціалізуватиПоля()

        керуванняСенсором = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        акселерометр = керуванняСенсором.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (акселерометр == null) {
            статусСенсора.text = "Акселерометр недоступний на цьому пристрої"
            полеРух.text = "Недоступно"
        } else {
            статусСенсора.text = "Акселерометр доступний"
        }

        savedInstanceState?.let {
            максимумGСила = it.getFloat(КЛЮЧ_МАКС_G, 0.0f)
            статусСенсора.text = it.getString(КЛЮЧ_СТАТУС, статусСенсора.text.toString())
            полеX.text = it.getString(КЛЮЧ_X, "X: 0.00")
            полеY.text = it.getString(КЛЮЧ_Y, "Y: 0.00")
            полеZ.text = it.getString(КЛЮЧ_Z, "Z: 0.00")
            полеПрискорення.text = it.getString(КЛЮЧ_ПРИСКОРЕННЯ, "Прискорення: 0.00 м/с²")
            полеGСила.text = it.getString(КЛЮЧ_GСИЛА, "G-сила: 0.00 g")
            полеПік.text = it.getString(КЛЮЧ_ПІК, "Пік: 0.00 g")
            полеРух.text = it.getString(КЛЮЧ_РУХ, "Очікування даних...")
            прогресGСила.progress = it.getInt(КЛЮЧ_ПРОГРЕС, 0)
        }
    }

    private fun ініціалізуватиПоля() {
        статусСенсора = findViewById(R.id.tvSensorStatus)
        полеX = findViewById(R.id.tvX)
        полеY = findViewById(R.id.tvY)
        полеZ = findViewById(R.id.tvZ)
        полеПрискорення = findViewById(R.id.tvAcceleration)
        полеGСила = findViewById(R.id.tvGForce)
        полеПік = findViewById(R.id.tvPeakValue)
        полеРух = findViewById(R.id.tvMotionStatus)
        прогресGСила = findViewById(R.id.progressGForce)
    }

    override fun onResume() {
        super.onResume()
        акселерометр?.let {
            керуванняСенсором.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        керуванняСенсором.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val прискорення = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gСила = прискорення / ЗЕМНЕ_ПРИСКОРЕННЯ

        if (gСила > максимумGСила) {
            максимумGСила = gСила
        }

        полеX.text = "X: %.2f".format(x)
        полеY.text = "Y: %.2f".format(y)
        полеZ.text = "Z: %.2f".format(z)

        полеПрискорення.text = "Прискорення: %.2f м/с²".format(прискорення)
        полеGСила.text = "G-сила: %.2f g".format(gСила)
        полеПік.text = "Пік: %.2f g".format(максимумGСила)

        val значенняПрогресу = (gСила * 100).toInt().coerceIn(0, 400)
        прогресGСила.progress = значенняПрогресу

        полеРух.text = when {
            gСила < 1.05f -> "Стабільно"
            gСила < 1.30f -> "Помірний рух"
            gСила < 2.00f -> "Сильний рух"
            else -> "Екстремальний рух"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat(КЛЮЧ_МАКС_G, максимумGСила)
        outState.putString(КЛЮЧ_СТАТУС, статусСенсора.text.toString())
        outState.putString(КЛЮЧ_X, полеX.text.toString())
        outState.putString(КЛЮЧ_Y, полеY.text.toString())
        outState.putString(КЛЮЧ_Z, полеZ.text.toString())
        outState.putString(КЛЮЧ_ПРИСКОРЕННЯ, полеПрискорення.text.toString())
        outState.putString(КЛЮЧ_GСИЛА, полеGСила.text.toString())
        outState.putString(КЛЮЧ_ПІК, полеПік.text.toString())
        outState.putString(КЛЮЧ_РУХ, полеРух.text.toString())
        outState.putInt(КЛЮЧ_ПРОГРЕС, прогресGСила.progress)
    }
}
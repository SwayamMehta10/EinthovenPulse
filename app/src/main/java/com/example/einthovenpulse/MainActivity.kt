package com.example.einthovenpulse

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Room
import com.example.einthovenpulse.ui.theme.EinthovenPulseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EinthovenPulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EinthovenPulseApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinthovenPulseApp() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                    )
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val navController = rememberNavController()
        val screens = Screens(innerPadding, navController)
        NavHost(navController, startDestination = "home_screen") {
            composable("home_screen") { screens.HomeScreen() }
            composable("heart_rate_screen") { screens.HeartRateScreen() }
            composable("respiratory_rate_screen") {
                screens.RespiratoryRateScreen()
            }
            composable("symptoms_screen") { screens.SymptomsScreen() }
        }
    }
}

class Screens(private val innerPadding: PaddingValues, private val navController: NavController) {
    private var heartRate = mutableIntStateOf(0)
    private val respiratoryRate = mutableIntStateOf(0)

    @Composable
    fun HomeScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedButton(onClick = {
                navController.navigate("heart_rate_screen")
            }) {
                Text(text = "Start Checkup")
            }
        }
    }

    @Composable
    fun HeartRateScreen() {
        val context = LocalContext.current
        val heartRateCalculator = MeasureHeartRate(context)
        val previewView = remember { PreviewView(context) }
        var isMeasuring by remember { mutableStateOf(false) }
        val isHeartRateMeasured = remember { mutableStateOf(false) }
        var rememberedHeartRate by remember { heartRate }
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    isMeasuring = true
                    heartRateCalculator.startCameraPreview(previewView)
                    heartRateCalculator.startRecording(context, previewView, isHeartRateMeasured)
                    heartRateCalculator.setHeartRateCallback { rate ->
                        rememberedHeartRate = rate // Update the heart rate state
                        isHeartRateMeasured.value = true // Mark as measured
                        isMeasuring = false // Mark as stopped
                    }
                } else {
                    Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Handle back navigation to reset state
        BackHandler {
            isHeartRateMeasured.value = false // Reset the heart rate measurement status
            Log.d("IsHeartRateMeasured", isHeartRateMeasured.toString())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Measure Heart Rate")
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("1. Click on Start Measuring.", textAlign = TextAlign.Justify)
                Text("2. Once the flashlight turns on, softly press your index finger on the camera lens while covering the flashlight.", textAlign = TextAlign.Justify)
                Text("3. Continue to do so for 45 seconds until the flashlight turns off.", textAlign = TextAlign.Justify)
            }

            Spacer(modifier = Modifier.height(32.dp))
            // Create a PreviewView instance
            AndroidView(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                factory = { previewView }
            )

            if (!isMeasuring) {
                Spacer(modifier = Modifier.height(32.dp))
                ElevatedButton(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        isMeasuring = true
                        heartRateCalculator.startCameraPreview(previewView)
                        heartRateCalculator.startRecording(
                            context,
                            previewView,
                            isHeartRateMeasured
                        )
                        heartRateCalculator.setHeartRateCallback { rate ->
                            rememberedHeartRate = rate // Update the heart rate state
                            isHeartRateMeasured.value = true // Mark as measured
                            isMeasuring = false // Mark as stopped
                        }
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }) {
                    Text(text = "Start Measuring")
                }
            } else {
                Spacer(modifier = Modifier.height(64.dp))
                Text(text = "Measuring... Please wait.")
            }

            if (isHeartRateMeasured.value) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Heart Rate: ${heartRate.intValue} BPM")
            }

            Spacer(modifier = Modifier.height(32.dp))
            ElevatedButton(
                onClick = {
                    if (isHeartRateMeasured.value) {
                        // Navigate to RespiratoryRateScreen
                        navController.navigate("respiratory_rate_screen")
                    } else {
                        Toast.makeText(
                            context,
                            "Please measure the heart rate first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = isHeartRateMeasured.value
            ) {
                Text(text = "Measure Respiratory Rate")
            }
        }
    }

    @Composable
    fun RespiratoryRateScreen() {
        val context = LocalContext.current
        val respiratoryRateSensor = remember { MeasureRespiratoryRate(context) }
        var isMeasuring by remember { mutableStateOf(false) }
        var rememberedRespiratoryRate by remember { respiratoryRate }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    // Start measuring if permission is granted
                    isMeasuring = true
                    respiratoryRateSensor.startMeasuring()
                } else {
                    Toast.makeText(context, "Body Sensor permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )

        // Effect to handle measurement completion
        LaunchedEffect(isMeasuring) {
            if (isMeasuring) {
                delay(45000L) // 45 seconds delay
                respiratoryRateSensor.stopMeasuring()
                rememberedRespiratoryRate = respiratoryRateSensor.calculateRespiratoryRate()
                isMeasuring = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Respiratory Rate Sensing Screen")
            Spacer(modifier = Modifier.height(64.dp))
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("1. Click on Start Measuring.", textAlign = TextAlign.Justify)
                Text("2. Lie down on your back and place your smartphone on your chest.", textAlign = TextAlign.Justify)
                Text("3. Take deep breaths in and out for a period of 45 seconds.", textAlign = TextAlign.Justify)
            }
            Spacer(modifier = Modifier.height(64.dp))

            if (!isMeasuring || rememberedRespiratoryRate > 0) {
                ElevatedButton(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.BODY_SENSORS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        isMeasuring = true
                        respiratoryRateSensor.startMeasuring()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
                    }
                }) {
                    Text("Start Measuring")
                }
            } else {
                Text(text = "Measuring... Please wait.")
            }

            if (rememberedRespiratoryRate > 0) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Respiratory Rate: ${respiratoryRate.intValue} breaths per minute")
            }

            Spacer(modifier = Modifier.height(32.dp))
            ElevatedButton(
                onClick = {
                    if (rememberedRespiratoryRate > 0) {
                        // Navigate to RespiratoryRateScreen
                        navController.navigate("symptoms_screen")
                    } else {
                        Toast.makeText(
                            context,
                            "Please measure the respiratory rate first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = rememberedRespiratoryRate > 0
            ) {
                Text(text = "Enter Symptoms")
            }
        }
    }

    @Composable
    fun SymptomsScreen() {
        val symptomRatings = remember { mutableStateMapOf<String, Int>() }
        val coroutineScope = rememberCoroutineScope()

        val db = Room.databaseBuilder(
            context = LocalContext.current,
            klass = SymptomsDatabase::class.java,
            name = "Symptoms Database"
        ).build()

        val measurementsDao = db.measurementsDao()
        var isUploaded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Rate the severity of your symptoms (whichever applicable):", textAlign = TextAlign.Justify, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp, end = 8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(symptoms) { symptom ->
                    SymptomItem(symptom, symptomRatings)
                }
            }

            ElevatedButton(onClick = {
                isUploaded = true
                coroutineScope.launch {
                    val measurements = Measurements(
                        heartRate = heartRate.intValue,
                        respiratoryRate = respiratoryRate.intValue,
                        nausea = symptomRatings["Nausea"] ?: 0,
                        headache = symptomRatings["Headache"] ?: 0,
                        diarrhea = symptomRatings["Diarrhea"] ?: 0,
                        soarThroat = symptomRatings["Soar Throat"] ?: 0,
                        fever = symptomRatings["Fever"] ?: 0,
                        muscleAche = symptomRatings["Muscle Ache"] ?: 0,
                        lossOSmellOrTaste = symptomRatings["Loss of Smell or Taste"] ?: 0,
                        cough = symptomRatings["Cough"] ?: 0,
                        shortnessOBreath = symptomRatings["Shortness of Breath"] ?: 0,
                        feelingTired = symptomRatings["Feeling Tired"] ?: 0
                    )
                    measurementsDao.submitMeasurements(measurements)
                }
            }, modifier = Modifier.padding(16.dp)) {
                Text(text = "Upload Symptoms")
            }

            if (isUploaded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Uploaded!")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EinthovenPulseAppPreview() {
    EinthovenPulseTheme {
        EinthovenPulseApp()
    }
}
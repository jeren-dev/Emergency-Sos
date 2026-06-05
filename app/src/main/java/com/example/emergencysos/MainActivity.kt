package com.example.emergencysos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var cameraManager: CameraManager

    private var latitude = 0.0
    private var longitude = 0.0
    private var isLocationAvailable = false

    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null

    private val capturedImageFiles = mutableListOf<File>()

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

            if (smsGranted && locationGranted && cameraGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "All Permissions Required", Toast.LENGTH_LONG).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseHelper = DatabaseHelper(this)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        requestPermissions()

        setContent {
            MaterialTheme {
                SOSDrawerConsole(
                    onCameraAlert = { captureAndSendEmail() },
                    onEmailAlert = { openEmailApp() },
                    onSmsAlert = { sendSMSWithLocation() },
                    onSirenAlert = { triggerSiren() },
                    onAddContact = { startActivity(Intent(this, AddContect::class.java)) },
                    onShowContact = { startActivity(Intent(this, ShowContect::class.java)) },
                    onTrackLocation = { startLocationTrackingAndSendSMS() }
                )
            }
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA
            )
        )
    }

    private fun getCurrentLocation(onLocationReceived: (() -> Unit)? = null) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    isLocationAvailable = true
                    Toast.makeText(this, "Location Matrix Synchronized", Toast.LENGTH_SHORT).show()
                    onLocationReceived?.invoke()
                } else {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val loc = result.lastLocation
                                if (loc != null) {
                                    latitude = loc.latitude
                                    longitude = loc.longitude
                                    isLocationAvailable = true
                                    fusedLocationClient.removeLocationUpdates(this)
                                    onLocationReceived?.invoke()
                                }
                            }
                        },
                        Looper.getMainLooper()
                    )
                }
            }
    }

    private fun sendSMSWithLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS Subsystem Disconnected", Toast.LENGTH_SHORT).show()
            return
        }

        getCurrentLocation {
            val contacts = databaseHelper.getAllContacts()
            if (contacts.isEmpty()) {
                Toast.makeText(this, "No Safe Contacts Provisioned", Toast.LENGTH_SHORT).show()
                return@getCurrentLocation
            }

            val smsManager = SmsManager.getDefault()
            val mapsLink = "https://maps.google.com/?q=$latitude,$longitude"
            val message = """
🚨 SOS EMERGENCY ALERT 🚨

I need help immediately.

📍 Live Location:
$mapsLink
            """.trimIndent()

            var successCount = 0
            contacts.forEach { contact ->
                try {
                    val number = contact.phone.trim()
                    if (number.isNotEmpty()) {
                        val parts = smsManager.divideMessage(message)
                        smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                        successCount++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Toast.makeText(this, "Telemetry Dispatched to $successCount Relays", Toast.LENGTH_LONG).show()
        }
    }

    private fun startLocationTrackingAndSendSMS() {
        getCurrentLocation {
            val locationMessage = """
📍 LIVE LOCATION TRACKING

Latitude:
$latitude

Longitude:
$longitude

https://maps.google.com/?q=$latitude,$longitude
            """.trimIndent()
            sendSMS(locationMessage)
        }
    }

    private fun sendSMS(message: String) {
        val contacts = databaseHelper.getAllContacts()
        val smsManager = SmsManager.getDefault()

        contacts.forEach {
            try {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(it.phone, null, parts, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerSiren() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.siren)
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openEmailApp() {
        getCurrentLocation {
            val contacts = databaseHelper.getAllContacts()
            val emailList = mutableListOf<String>()

            contacts.forEach {
                if (it.email.isNotEmpty()) {
                    emailList.add(it.email)
                }
            }

            if (emailList.isEmpty()) {
                Toast.makeText(this, "No Registered Relay Emails Found", Toast.LENGTH_SHORT).show()
                return@getCurrentLocation
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, emailList.toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, "CRITICAL SOS BROADCAST")
                putExtra(Intent.EXTRA_TEXT, "🚨 CRITICAL MEDICAL BROADCAST 🚨\n\nGeographic Context:\nhttps://maps.google.com/?q=$latitude,$longitude")
            }
            startActivity(intent)
        }
    }

    private fun captureAndSendEmail() {
        try {
            getCurrentLocation()
            val frontCameraId = getFrontCameraId()
            if (frontCameraId != null) {
                openCamera(frontCameraId)
            } else {
                Toast.makeText(this, "Optical Array Inaccessible", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFrontCameraId(): String? {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }
        return null
    }

    private fun openCamera(cameraId: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        cameraManager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            },
            Handler(Looper.getMainLooper())
        )
    }

    private fun createCaptureSession() {
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                saveImage(bitmap)
                image.close()
            }, Handler(Looper.getMainLooper()))
        }

        val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
            addTarget(imageReader!!.surface)
        }

        cameraDevice?.createCaptureSession(
            listOf(imageReader!!.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.capture(requestBuilder!!.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            null
        )
    }

    private fun saveImage(bitmap: Bitmap) {
        try {
            val file = File.createTempFile("sos_payload_", ".jpg", cacheDir)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()

            capturedImageFiles.clear()
            capturedImageFiles.add(file)

            openEmailWithAttachments()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openEmailWithAttachments() {
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "SURVEILLANCE PAYLOAD ATTACHMENT")
            putExtra(Intent.EXTRA_TEXT, "🚨 EN ROUTE PAYLOAD ACQUIRED 🚨\n\nCoordinates:\nhttps://maps.google.com/?q=$latitude,$longitude")
        }

        val uris = ArrayList<Uri>().apply {
            capturedImageFiles.forEach {
                add(FileProvider.getUriForFile(this@MainActivity, "${packageName}.provider", it))
            }
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(emailIntent, "Transmit Payload via:"))
    }
}

// UI Framework Data Layout Map
data class LuxuryActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSDrawerConsole(
    onCameraAlert: () -> Unit,
    onEmailAlert: () -> Unit,
    onSmsAlert: () -> Unit,
    onSirenAlert: () -> Unit,
    onAddContact: () -> Unit,
    onShowContact: () -> Unit,
    onTrackLocation: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF09110E),
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Control Center",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "System Configuration Node",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(24.dp))

                    DrawerNavButton(
                        text = "Configure Relays",
                        subtitle = "Provision new hardware details",
                        icon = Icons.Default.PersonAdd,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAddContact()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DrawerNavButton(
                        text = "Relay Network",
                        subtitle = "Verify response nodes",
                        icon = Icons.Default.People,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onShowContact()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SHIELD CONSOLE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onTrackLocation) {
                            Icon(
                                imageVector = Icons.Default.LocationSearching,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF070D0B)
                    )
                )
            }
        ) { paddingValues ->

            val backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070D0B),
                    Color(0xFF0C1A14),
                    Color(0xFF050A08)
                )
            )

            val dashboardActions = listOf(
                LuxuryActionItem(
                    title = "Capture Vector",
                    subtitle = "Optical Payload Trigger",
                    icon = Icons.Default.CameraAlt,
                    primaryColor = Color(0xFF10B981),
                    onClick = onCameraAlert
                ),
                LuxuryActionItem(
                    title = "Secure Email",
                    subtitle = "Broadcast Encrypted Mail",
                    icon = Icons.Default.Email,
                    primaryColor = Color(0xFF059669),
                    onClick = onEmailAlert
                ),
                LuxuryActionItem(
                    title = "Emergency SMS",
                    subtitle = "Transmit Network SMS",
                    icon = Icons.Default.Message,
                    primaryColor = Color(0xFF34D399),
                    onClick = onSmsAlert
                ),
                LuxuryActionItem(
                    title = "Audio Beacon",
                    subtitle = "Deploy Acoustic Siren",
                    icon = Icons.Default.NotificationsActive,
                    primaryColor = Color(0xFF0284C7),
                    onClick = onSirenAlert
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "EMERGENCY PROTOCOL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Emergency SOS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Select any node below to initiate an immediate secure threat broadcast sequence.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(dashboardActions) { component ->
                            PremiumGlasswareCard(
                                title = component.title,
                                subtitle = component.subtitle,
                                icon = component.icon,
                                overlayColor = component.primaryColor,
                                onClick = component.onClick
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    InteractiveRadarBanner(onTrackerAction = onTrackLocation)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumGlasswareCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    overlayColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Dynamic spring matrix scaling mechanism replicating smooth web engines
    val dynamicScaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "MatrixScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
            .graphicsLayer {
                scaleX = dynamicScaleFactor
                scaleY = dynamicScaleFactor
            }
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(overlayColor.copy(alpha = 0.12f))
                    .border(1.dp, overlayColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = overlayColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun DrawerNavButton(
    text: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val dynamicScaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "DrawerNavScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = dynamicScaleFactor
                scaleY = dynamicScaleFactor
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF10B981).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun InteractiveRadarBanner(onTrackerAction: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val dynamicScaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "BannerScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = dynamicScaleFactor
                scaleY = dynamicScaleFactor
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))
                )
            )
            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onTrackerAction() }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live Radar Pulse Active",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Broadcast ongoing telemetry feeds instantly over available secure background bands.",
                    color = Color(0xFFA7F3D0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
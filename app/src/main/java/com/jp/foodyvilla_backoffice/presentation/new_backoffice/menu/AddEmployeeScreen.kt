package com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeAdminUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.EmployeeRole
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.OutletResponse
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.DialogType
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.OperationResultDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.utils.ZoomableImagePreviewDialog
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.EmployeeAdminViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    employeeId: Long? = null,
    viewModel: EmployeeAdminViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isEditMode = employeeId != null

    // Screen Form States
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var selectedOutlet by remember { mutableStateOf<OutletResponse?>(null) }
    var selectedRole by remember { mutableStateOf(EmployeeRole.EMPLOYEE) }

    var shiftStartHour by remember { mutableStateOf<Int?>(null) }
    var shiftStartMinute by remember { mutableStateOf<Int?>(null) }
    var shiftEndHour by remember { mutableStateOf<Int?>(null) }
    var shiftEndMinute by remember { mutableStateOf<Int?>(null) }

    var salaryInput by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var joiningDateMillis by remember { mutableStateOf<Long?>(null) }
    var isActive by remember { mutableStateOf(true) }

    // Media State
    var profilePhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profilePhotoBitmapPreview by remember { mutableStateOf<Bitmap?>(null) }
    var existingProfileUrl by remember { mutableStateOf("") }

    var aadhaarPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var aadhaarPhotoBitmapPreview by remember { mutableStateOf<Bitmap?>(null) }
    var existingAadhaarUrl by remember { mutableStateOf("") }

    // Image Zoom Preview States
    var activePreviewTitle by remember { mutableStateOf("") }
    var activePreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activePreviewUrl by remember { mutableStateOf<String?>(null) }
    var activePreviewBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImagePreviewDialog by remember { mutableStateOf(false) }

    // Dialog & Picker States
    var isSubmitting by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var resultDialogType by remember { mutableStateOf<DialogType?>(null) }
    var resultDialogTitle by remember { mutableStateOf("") }
    var resultDialogMessage by remember { mutableStateOf("") }
    var shouldNavigateBackOnDismiss by remember { mutableStateOf(false) }

    // Dropdown States
    var outletDropdownExpanded by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllEmployeesDirectory()
    }

    // Populate existing values when in Edit Mode
    LaunchedEffect(employeeId, state.employeesList, state.cachedOutlets) {
        if (employeeId != null) {
            val target = state.employeesList.find { it.id == employeeId }
            if (target != null) {
                name = target.name
                phone = target.contact
                password = target.passwordText
                selectedOutlet = state.cachedOutlets.find { it.id == target.outletId }
                selectedRole = target.role

                val startLocal = parseTimeStr(target.punchInTime)
                if (startLocal != null) {
                    shiftStartHour = startLocal.hour
                    shiftStartMinute = startLocal.minute
                }

                val endLocal = parseTimeStr(target.punchOutTime)
                if (endLocal != null) {
                    shiftEndHour = endLocal.hour
                    shiftEndMinute = endLocal.minute
                }

                salaryInput = target.salary
                address = target.address
                emergencyContact = target.emergencyContact
                joiningDateMillis = parseDateStrToMillis(target.joiningDate)
                isActive = target.isActive
                existingProfileUrl = target.profileImgUrl
                existingAadhaarUrl = target.aadharNo
            }
        }
    }

    // Camera & Gallery Launchers for Profile Photo
    val profileGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bytes = compressImageToMaxBytes(context, uri, 500 * 1024)
            if (bytes != null) {
                profilePhotoBytes = bytes
                profilePhotoBitmapPreview = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                resultDialogType = DialogType.ERROR
                resultDialogTitle = "Image Error"
                resultDialogMessage = "Unable to process selected profile image."
            }
        }
    }

    val profileCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val bytes = compressBitmapToMaxBytes(bitmap, 500 * 1024)
            if (bytes != null) {
                profilePhotoBytes = bytes
                profilePhotoBitmapPreview = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }

    // Camera & Gallery Launchers for Aadhaar Card
    val aadhaarGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bytes = compressImageToMaxBytes(context, uri, 500 * 1024)
            if (bytes != null) {
                aadhaarPhotoBytes = bytes
                aadhaarPhotoBitmapPreview = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                resultDialogType = DialogType.ERROR
                resultDialogTitle = "Image Error"
                resultDialogMessage = "Unable to process selected Aadhaar document."
            }
        }
    }

    val aadhaarCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val bytes = compressBitmapToMaxBytes(bitmap, 500 * 1024)
            if (bytes != null) {
                aadhaarPhotoBytes = bytes
                aadhaarPhotoBitmapPreview = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) "Edit Employee" else "Add Employee",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditMode) "Update employee profile and work details" else "Add a new employee to your team",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. PROFILE PHOTO SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Profile Photo")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (profilePhotoBitmapPreview != null || existingProfileUrl.isNotBlank()) {
                                    activePreviewTitle = "Profile Photo"
                                    activePreviewBitmap = profilePhotoBitmapPreview
                                    activePreviewBytes = profilePhotoBytes
                                    activePreviewUrl = existingProfileUrl.ifBlank { null }
                                    showImagePreviewDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoBitmapPreview != null) {
                            androidx.compose.foundation.Image(
                                bitmap = profilePhotoBitmapPreview!!.asImageBitmap(),
                                contentDescription = "Profile Photo Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (existingProfileUrl.isNotBlank()) {
                            AsyncImage(
                                model = existingProfileUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (profilePhotoBitmapPreview != null || existingProfileUrl.isNotBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { profileGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Change")
                            }
                            TextButton(
                                onClick = {
                                    profilePhotoBytes = null
                                    profilePhotoBitmapPreview = null
                                    existingProfileUrl = ""
                                }
                            ) {
                                Text("Remove", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { profileCameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo")
                            }

                            OutlinedButton(
                                onClick = { profileGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Gallery")
                            }
                        }
                    }

                    Text(
                        text = "Max 500 KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // -----------------------------------------------------------------
            // 2. BASIC INFORMATION SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Basic Information")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                placeholder = { Text("Enter full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                placeholder = { Text("Enter phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                placeholder = { Text("Set a password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // -----------------------------------------------------------------
            // 3. IDENTITY DOCUMENT SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Identity Document")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Aadhaar Card", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Upload Aadhaar card image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (aadhaarPhotoBitmapPreview != null || existingAadhaarUrl.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable {
                                    if (aadhaarPhotoBitmapPreview != null || existingAadhaarUrl.isNotBlank()) {
                                        activePreviewTitle = "Aadhaar Card"
                                        activePreviewBitmap = aadhaarPhotoBitmapPreview
                                        activePreviewBytes = aadhaarPhotoBytes
                                        activePreviewUrl = existingAadhaarUrl.takeIf { it.startsWith("http") }
                                        showImagePreviewDialog = true
                                    }
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (aadhaarPhotoBitmapPreview != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = aadhaarPhotoBitmapPreview!!.asImageBitmap(),
                                    contentDescription = "Aadhaar Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (existingAadhaarUrl.startsWith("http")) {
                                AsyncImage(
                                    model = existingAadhaarUrl,
                                    contentDescription = "Aadhaar Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = existingAadhaarUrl,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { aadhaarGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Change")
                            }
                            TextButton(
                                onClick = {
                                    aadhaarPhotoBytes = null
                                    aadhaarPhotoBitmapPreview = null
                                    existingAadhaarUrl = ""
                                }
                            ) {
                                Text("Remove", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { aadhaarCameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo")
                            }

                            OutlinedButton(
                                onClick = { aadhaarGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Gallery")
                            }
                        }
                    }

                    Text(
                        text = "Max 500 KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // -----------------------------------------------------------------
            // 4. WORK DETAILS SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Work Details")

            // Outlet Dropdown
            ExposedDropdownMenuBox(
                expanded = outletDropdownExpanded,
                onExpandedChange = { outletDropdownExpanded = !outletDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedOutlet?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Outlet *") },
                    placeholder = { Text("Select outlet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outletDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = outletDropdownExpanded,
                    onDismissRequest = { outletDropdownExpanded = false }
                ) {
                    state.cachedOutlets.forEach { outletItem ->
                        DropdownMenuItem(
                            text = { Text(outletItem.name) },
                            onClick = {
                                selectedOutlet = outletItem
                                outletDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Role Dropdown
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
            ) {
                val roleDisplay = when (selectedRole) {
                    EmployeeRole.EMPLOYEE -> "Employee"
                    EmployeeRole.HEAD -> "Manager"
                    EmployeeRole.OWNER -> "Owner"
                    EmployeeRole.CHEF -> "Chef"
                }

                OutlinedTextField(
                    value = roleDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role *") },
                    placeholder = { Text("Select role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    listOf(
                        EmployeeRole.EMPLOYEE to "Employee",
                        EmployeeRole.HEAD to "Manager",
                        EmployeeRole.CHEF to "Chef",
                        EmployeeRole.OWNER to "Owner"
                    ).forEach { (roleEnum, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedRole = roleEnum
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 5. SHIFT TIME SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Shift Time")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time Field Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Shift Start Time *",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val startTimeText = if (shiftStartHour != null && shiftStartMinute != null) {
                                formatHourMinuteDisplay(shiftStartHour!!, shiftStartMinute!!)
                            } else "09:00 AM"
                            Text(
                                text = startTimeText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // End Time Field Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Shift End Time *",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val endTimeText = if (shiftEndHour != null && shiftEndMinute != null) {
                                formatHourMinuteDisplay(shiftEndHour!!, shiftEndMinute!!)
                            } else "06:00 PM"
                            Text(
                                text = endTimeText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 6. SALARY SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Salary")

            OutlinedTextField(
                value = salaryInput,
                onValueChange = { salaryInput = it },
                label = { Text("Monthly Salary (INR)") },
                placeholder = { Text("Enter amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            // -----------------------------------------------------------------
            // 7. OTHER DETAILS SECTION
            // -----------------------------------------------------------------
            FormSectionHeader(title = "Other Details")

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                placeholder = { Text("Enter residential address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text("Emergency Contact") },
                placeholder = { Text("Enter emergency contact number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )

            // Joining Date Clickable Field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Joining Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val joiningDateText = joiningDateMillis?.let { formatDateMillisDisplay(it) } ?: "Select date"
                        Text(
                            text = joiningDateText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (joiningDateMillis != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 8. EMPLOYEE STATUS SECTION
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Employee",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // -----------------------------------------------------------------
            // 9. PRIMARY ACTION BUTTON
            // -----------------------------------------------------------------
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || password.isBlank() || selectedOutlet == null) {
                        resultDialogType = DialogType.ERROR
                        resultDialogTitle = "Validation Error"
                        resultDialogMessage = "Please complete all mandatory fields: Full Name, Phone Number, Password, and Outlet."
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            var uploadedPhotoUrl = existingProfileUrl
                            if (profilePhotoBytes != null) {
                                uploadedPhotoUrl = viewModel.uploadImageBytes(profilePhotoBytes!!, "profile")
                            }

                            var uploadedAadhaarUrl = existingAadhaarUrl
                            if (aadhaarPhotoBytes != null) {
                                uploadedAadhaarUrl = viewModel.uploadImageBytes(aadhaarPhotoBytes!!, "aadhaar")
                            }

                            val startStr = if (shiftStartHour != null && shiftStartMinute != null) {
                                formatHourMinuteDb(shiftStartHour!!, shiftStartMinute!!)
                            } else "09:00:00"

                            val endStr = if (shiftEndHour != null && shiftEndMinute != null) {
                                formatHourMinuteDb(shiftEndHour!!, shiftEndMinute!!)
                            } else "18:00:00"

                            val joiningDateDb = joiningDateMillis?.let { formatDateMillisDb(it) } ?: ""

                            val payload = EmployeeAdminUiModel(
                                id = employeeId ?: 0L,
                                outletId = selectedOutlet?.id,
                                name = name.trim(),
                                contact = phone.trim(),
                                passwordText = password.trim(),
                                address = address.trim(),
                                salary = salaryInput.trim(),
                                emergencyContact = emergencyContact.trim(),
                                joiningDate = joiningDateDb,
                                role = selectedRole,
                                isActive = isActive,
                                punchInTime = startStr,
                                punchOutTime = endStr,
                                profileImgUrl = uploadedPhotoUrl,
                                aadharNo = uploadedAadhaarUrl.ifBlank { "[Aadhaar Redacted]" }
                            )

                            if (isEditMode) {
                                viewModel.updateEmployeeDirect(payload, uploadedPhotoUrl)
                            } else {
                                viewModel.createEmployeeDirect(payload, uploadedPhotoUrl)
                            }

                            isSubmitting = false
                            shouldNavigateBackOnDismiss = true
                            resultDialogType = DialogType.SUCCESS
                            resultDialogTitle = "Success"
                            resultDialogMessage = if (isEditMode) {
                                "Employee profile for '$name' updated successfully!"
                            } else {
                                "Employee profile for '$name' created successfully!"
                            }
                        } catch (e: Exception) {
                            isSubmitting = false
                            resultDialogType = DialogType.ERROR
                            resultDialogTitle = if (isEditMode) "Update Failed" else "Creation Failed"
                            resultDialogMessage = e.localizedMessage ?: "Failed to save employee record."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Update Employee" else "Create Employee",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // =================================--------------------------------====
        // DIALOGS & PICKERS
        // =====================================================================

        // Time Picker for Shift Start Time
        if (showStartTimePicker) {
            MaterialTimePickerDialog(
                title = "Select Shift Start Time",
                initialHour = shiftStartHour ?: 9,
                initialMinute = shiftStartMinute ?: 0,
                onDismiss = { showStartTimePicker = false },
                onTimeSelected = { hour, minute ->
                    shiftStartHour = hour
                    shiftStartMinute = minute
                }
            )
        }

        // Time Picker for Shift End Time
        if (showEndTimePicker) {
            MaterialTimePickerDialog(
                title = "Select Shift End Time",
                initialHour = shiftEndHour ?: 18,
                initialMinute = shiftEndMinute ?: 0,
                onDismiss = { showEndTimePicker = false },
                onTimeSelected = { hour, minute ->
                    shiftEndHour = hour
                    shiftEndMinute = minute
                }
            )
        }

        // Date Picker for Joining Date
        if (showDatePicker) {
            MaterialDatePickerDialog(
                title = "Select Joining Date",
                onDismiss = { showDatePicker = false },
                onDateSelected = { millis ->
                    joiningDateMillis = millis
                }
            )
        }

        // Full Screen Zoomable Image Preview
        if (showImagePreviewDialog) {
            ZoomableImagePreviewDialog(
                title = activePreviewTitle,
                bitmap = activePreviewBitmap,
                imageBytes = activePreviewBytes,
                imageUrl = activePreviewUrl,
                onDismiss = { showImagePreviewDialog = false }
            )
        }

        // Result Success / Error Dialog
        resultDialogType?.let { type ->
            OperationResultDialog(
                type = type,
                title = resultDialogTitle,
                message = resultDialogMessage,
                onDismiss = {
                    resultDialogType = null
                    if (shouldNavigateBackOnDismiss) {
                        onNavigateBack()
                    }
                }
            )
        }
    }
}

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialTimePickerDialog(
    title: String,
    initialHour: Int = 9,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(state.hour, state.minute)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(state = state)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialDatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (millis: Long) -> Unit
) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onDateSelected(it) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state, title = { Text(title, modifier = Modifier.padding(16.dp)) })
    }
}

private fun parseTimeStr(timeStr: String?): LocalTime? {
    if (timeStr.isNullOrBlank()) return null
    return try {
        val clean = timeStr.split("+")[0].split("-")[0].replace("Z", "").trim()
        val parts = clean.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        LocalTime.of(hour, minute)
    } catch (e: Exception) {
        null
    }
}

private fun parseDateStrToMillis(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        val clean = dateStr.trim().split("T")[0]
        val localDate = LocalDate.parse(clean, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (e: Exception) {
        null
    }
}

private fun compressImageToMaxBytes(context: Context, uri: Uri, maxBytes: Int = 500 * 1024): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        compressBitmapToMaxBytes(originalBitmap, maxBytes)
    } catch (e: Exception) {
        null
    }
}

private fun compressBitmapToMaxBytes(bitmap: Bitmap, maxBytes: Int = 500 * 1024): ByteArray? {
    return try {
        var width = bitmap.width
        var height = bitmap.height
        val maxDimension = 1200
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxDimension
                height = (maxDimension / ratio).toInt()
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt()
            }
        }
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        var quality = 85
        var stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        while (stream.toByteArray().size > maxBytes && quality > 15) {
            quality -= 10
            stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }
        stream.toByteArray()
    } catch (e: Exception) {
        null
    }
}

private fun formatHourMinuteDisplay(hour: Int, minute: Int): String {
    val localTime = LocalTime.of(hour, minute)
    return localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
}

private fun formatHourMinuteDb(hour: Int, minute: Int): String {
    val localTime = LocalTime.of(hour, minute)
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))
}

private fun formatDateMillisDisplay(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
}

private fun formatDateMillisDb(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
}

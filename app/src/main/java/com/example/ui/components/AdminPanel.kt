package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Sponsor
import com.example.ui.viewmodel.IptvUiState
import java.util.UUID

@Composable
fun AdminPanelDialog(
    state: IptvUiState,
    onDismiss: () -> Unit,
    onPasswordSubmit: (String) -> Unit,
    onLogout: () -> Unit,
    onSaveSponsor: (Sponsor) -> Unit,
    onDeleteSponsor: (String) -> Unit,
    onEditSponsorClick: (Sponsor?) -> Unit,
    onAddAdmin: (String) -> Unit,
    onRemoveAdmin: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(600.dp)
                .fillMaxHeight(0.9f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            color = Color(0xFF0F172A) // Dark Slate
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NeoTV Pro - Hybrid Cloud Controller",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("admin_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

                if (!state.isAdminLoggedIn) {
                    // Password Challenge Form
                    PasswordChallengeForm(
                        onSubmit = onPasswordSubmit,
                        developerEmail = "foridahmed6682@gmail.com"
                    )
                } else {
                    // Logged In Admin Form
                    AdminSponsorsManager(
                        state = state,
                        onLogout = onLogout,
                        onSaveSponsor = onSaveSponsor,
                        onDeleteSponsor = onDeleteSponsor,
                        onEditSponsorClick = onEditSponsorClick,
                        onAddAdmin = onAddAdmin,
                        onRemoveAdmin = onRemoveAdmin
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordChallengeForm(
    onSubmit: (String) -> Unit,
    developerEmail: String
) {
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DEVELOPER ACCESS RESTRICTED",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Owner/Reviewer: $developerEmail",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Developer Security Code") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_password_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Text(
            text = "Hint: enter code 'faridahmed' or 'admin6682' to authorize.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        SpacerHeight(16)

        Button(
            onClick = { onSubmit(password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_submit_password_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Authorize Access", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminSponsorsManager(
    state: IptvUiState,
    onLogout: () -> Unit,
    onSaveSponsor: (Sponsor) -> Unit,
    onDeleteSponsor: (String) -> Unit,
    onEditSponsorClick: (Sponsor?) -> Unit,
    onAddAdmin: (String) -> Unit,
    onRemoveAdmin: (String) -> Unit
) {
    var imageUrl by remember(state.editingSponsor) { mutableStateOf(state.editingSponsor?.imageUrl ?: "") }
    var text by remember(state.editingSponsor) { mutableStateOf(state.editingSponsor?.text ?: "") }
    var linkUrl by remember(state.editingSponsor) { mutableStateOf(state.editingSponsor?.linkUrl ?: "") }
    var isActive by remember(state.editingSponsor) { mutableStateOf(state.editingSponsor?.isActive ?: true) }

    var adminEmailInput by remember { mutableStateOf("") }
    var showAdminManager by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAdminManager) "Manage Authorized Admins" else if (state.editingSponsor != null) "Edit Sponsor" else "Add New Sponsor",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showAdminManager = !showAdminManager }) {
                    Icon(
                        imageVector = if (showAdminManager) Icons.Default.Edit else Icons.Default.PersonAdd,
                        contentDescription = "Toggle Manager",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            SpacerHeight(8)
            HorizontalDivider(color = Color(0xFF1E293B))
            SpacerHeight(12)
        }

        if (showAdminManager) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = adminEmailInput,
                        onValueChange = { adminEmailInput = it },
                        label = { Text("New Admin Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (adminEmailInput.isNotEmpty()) {
                                    onAddAdmin(adminEmailInput)
                                    adminEmailInput = ""
                                }
                            }) {
                                Icon(Icons.Default.PersonAdd, "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    
                    SpacerHeight(12)
                    
                    if (state.isSyncingAdmins) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                    }
                    
                    Text("Authorized Admin Emails:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    SpacerHeight(4)
                }
            }
            
            items(state.adminEmails) { email ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(email, modifier = Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    if (email != "foridahmed6682@gmail.com") {
                        IconButton(onClick = { onRemoveAdmin(email) }) {
                            Icon(Icons.Default.PersonRemove, "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        } else {
            // Original Sponsors Manager UI
            item {
                // Input Fields
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Sponsor Logo Image URL") },
                    placeholder = { Text("https://example.com/logo.png") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sponsor_image_url_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                SpacerHeight(8)

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Sponsor Tagline / Bengali & English Text") },
                    placeholder = { Text("Premium Sponsor corporate advertisement details here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sponsor_text_input"),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )

                SpacerHeight(8)

                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("External Sponsor Promotion URL Link") },
                    placeholder = { Text("https://sponsorweb.com/offer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sponsor_link_url_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                SpacerHeight(12)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sponsor Advertisement Active visibility:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("sponsor_visibility_switch")
                    )
                }

                SpacerHeight(12)

                Row(modifier = Modifier.fillMaxWidth()) {
                    if (state.editingSponsor != null) {
                        Button(
                            onClick = { onEditSponsorClick(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel")
                        }
                    }

                    Button(
                        onClick = {
                            val newSponsor = Sponsor(
                                id = state.editingSponsor?.id ?: "sponsor_${UUID.randomUUID()}",
                                imageUrl = imageUrl,
                                text = text,
                                linkUrl = linkUrl,
                                isActive = isActive,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveSponsor(newSponsor)
                            // Reset local forms
                            imageUrl = ""
                            text = ""
                            linkUrl = ""
                            isActive = true
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                            .testTag("sponsor_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (state.editingSponsor != null) "Update Sponsor" else "Publish Sponsor", fontWeight = FontWeight.Bold)
                    }
                }

                SpacerHeight(16)
                HorizontalDivider(color = Color(0xFF1E293B))
                SpacerHeight(12)

                Text(
                    text = "Active Sponsored Campaigns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                SpacerHeight(8)
            }

            if (state.listSponsorsForAdmin.isEmpty()) {
                item {
                    Text(
                        text = "No sponsor campaigns loaded. Add one above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            items(state.listSponsorsForAdmin) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(0.5.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo Preview
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        SpacerWidth(8)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = if (item.isActive) "Visible • Active" else "Hidden • Suspended",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isActive) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        // Edit
                        IconButton(
                            onClick = { onEditSponsorClick(item) },
                            modifier = Modifier.testTag("edit_sponsor_${item.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray)
                        }

                        // Delete
                        IconButton(
                            onClick = { onDeleteSponsor(item.id) },
                            modifier = Modifier.testTag("delete_sponsor_${item.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpacerHeight(dp: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(dp.dp))
}

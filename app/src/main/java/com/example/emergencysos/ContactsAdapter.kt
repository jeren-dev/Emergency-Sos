package com.example.emergencysos

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactsScreen(
    contactsList: ArrayList<Contact>,
    databaseHelper: DatabaseHelper
) {
    val context = LocalContext.current

    // Synchronizes flawlessly with your exact snapshot mutation layout tracking
    val contacts = remember {
        mutableStateListOf<Contact>().apply {
            addAll(contactsList)
        }
    }

    // Unified deep premium dark glass background matrix
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070D0B),
            Color(0xFF0C1A14),
            Color(0xFF050A08)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 24.dp)
    ) {
        if (contacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactEmergency,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Network Hub Empty",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No emergency nodes mapped. Swipe open your control drawer terminal to authorize active relays.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = "Configured Relays",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "These active tracking destinations will collect data payloads when an SOS is tripped.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                itemsIndexed(contacts) { index, contact ->
                    ContactItem(
                        contact = contact,
                        onDelete = {
                            val isDeleted = databaseHelper.deleteContact(contact.id)
                            if (isDeleted) {
                                contacts.removeAt(index)
                                Toast.makeText(context, "Relay Node De-authorized", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Termination sequence failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth dynamic spring pressing simulation layout
    val fluidCardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "ListMetricScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = fluidCardScale
                scaleY = fluidCardScale
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Hook for custom node analytics tools */ },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactile Monogram Emblem replacing simple default layouts
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))

                        // Micro Network Active Tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text(
                                text = "READY LINK",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // High-End minimalist contextual deletion engine target
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "De-authorize node",
                        tint = Color(0xFFEF4444).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-profile communication hardware node details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ContactMetadataComponent(icon = Icons.Default.Phone, data = contact.phone)
                ContactMetadataComponent(icon = Icons.Default.Email, data = contact.email)
            }
        }
    }
}

@Composable
fun ContactMetadataComponent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    data: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = data.ifEmpty { "Not Provisioned" },
            fontSize = 13.sp,
            color = if (data.isNotEmpty()) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
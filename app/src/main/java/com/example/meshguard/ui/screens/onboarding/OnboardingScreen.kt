package com.example.meshguard.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.R
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.ColorSurfaceElevatedDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.RescuerBadgeBlue
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.OnboardingUiState
import com.example.meshguard.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingRoute(
    onNavigateToPermissions: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRoleConfirmed) {
        if (uiState.isRoleConfirmed) {
            onNavigateToPermissions()
        }
    }

    OnboardingScreen(
        uiState = uiState,
        onNextClicked = viewModel::onNextClicked,
        onSkipClicked = viewModel::onSkipClicked,
        onSlideChanged = viewModel::onSlideChanged,
        onSelectRole = viewModel::onSelectRole,
        onRescuerAuthCodeChanged = viewModel::onRescuerAuthCodeChanged,
        onConfirmRole = viewModel::onConfirmRole
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNextClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onSlideChanged: (Int) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onRescuerAuthCodeChanged: (String) -> Unit,
    onConfirmRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = uiState.currentSlideIndex) { 3 }

    LaunchedEffect(uiState.currentSlideIndex) {
        if (pagerState.currentPage != uiState.currentSlideIndex) {
            pagerState.animateScrollToPage(uiState.currentSlideIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (uiState.currentSlideIndex != pagerState.currentPage) {
            onSlideChanged(pagerState.currentPage)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ColorBackgroundDark
    ) {
        AnimatedVisibility(
            visible = !uiState.isShowingRoleSelection,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmergencyRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = TextPrimary
                        )
                    }

                    TextButton(
                        onClick = onSkipClicked,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_action_skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { page ->
                    when (page) {
                        0 -> OnboardingSlideContent(
                            icon = Icons.Default.CellTower,
                            iconTint = EmergencyRed,
                            title = stringResource(R.string.onboarding_slide1_title),
                            description = stringResource(R.string.onboarding_slide1_desc),
                            tag = stringResource(R.string.onboarding_slide1_tag)
                        )
                        1 -> OnboardingSlideContent(
                            icon = Icons.Default.Hub,
                            iconTint = MeshCyan,
                            title = stringResource(R.string.onboarding_slide2_title),
                            description = stringResource(R.string.onboarding_slide2_desc),
                            tag = stringResource(R.string.onboarding_slide2_tag)
                        )
                        2 -> OnboardingSlideContent(
                            icon = Icons.Default.Shield,
                            iconTint = EmergencyGreen,
                            title = stringResource(R.string.onboarding_slide3_title),
                            description = stringResource(R.string.onboarding_slide3_desc),
                            tag = stringResource(R.string.onboarding_slide3_tag)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        for (i in 0 until uiState.totalSlides) {
                            val isSelected = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 28.dp else 8.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) MeshCyan else ColorSurfaceBorder)
                            )
                        }
                    }

                    Button(
                        onClick = onNextClicked,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pagerState.currentPage == uiState.totalSlides - 1) EmergencyRed else ColorSurfaceElevatedDark,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (pagerState.currentPage == uiState.totalSlides - 1) EmergencyRed else MeshCyan,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (pagerState.currentPage == uiState.totalSlides - 1) {
                                    stringResource(R.string.onboarding_action_select_role)
                                } else {
                                    stringResource(R.string.action_next)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isShowingRoleSelection,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = fadeOut()
        ) {
            RoleSelectionContent(
                selectedRole = uiState.selectedRole,
                rescuerAuthCode = uiState.rescuerAuthCode,
                authCodeErrorRes = uiState.authCodeErrorRes,
                onSelectRole = onSelectRole,
                onRescuerAuthCodeChanged = onRescuerAuthCodeChanged,
                onConfirmRole = onConfirmRole
            )
        }
    }
}

@Composable
private fun OnboardingSlideContent(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    tag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(ColorSurfaceElevatedDark)
                .border(2.dp, iconTint.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = iconTint.copy(alpha = 0.15f)
        ) {
            Text(
                text = tag.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = iconTint,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun RoleSelectionContent(
    selectedRole: UserRole,
    rescuerAuthCode: String,
    authCodeErrorRes: Int?,
    onSelectRole: (UserRole) -> Unit,
    onRescuerAuthCodeChanged: (String) -> Unit,
    onConfirmRole: () -> Unit
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.role_screen_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MeshCyan,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.role_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.role_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoleOptionCard(
                role = UserRole.SURVIVOR,
                title = stringResource(R.string.role_survivor_title),
                description = stringResource(R.string.role_survivor_desc),
                icon = Icons.Default.Person,
                badgeColor = MeshCyan,
                isSelected = selectedRole == UserRole.SURVIVOR,
                onClick = { onSelectRole(UserRole.SURVIVOR) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleOptionCard(
                role = UserRole.RESCUER,
                title = stringResource(R.string.role_rescuer_title),
                description = stringResource(R.string.role_rescuer_desc),
                icon = Icons.Default.Shield,
                badgeColor = RescuerBadgeBlue,
                isSelected = selectedRole == UserRole.RESCUER,
                onClick = { onSelectRole(UserRole.RESCUER) }
            )

            AnimatedVisibility(
                visible = selectedRole == UserRole.RESCUER,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.role_rescuer_code_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RescuerBadgeBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = rescuerAuthCode,
                        onValueChange = onRescuerAuthCodeChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Rescuer authorization token input"
                            },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.role_rescuer_auth_hint),
                                color = TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RescuerBadgeBlue
                            )
                        },
                        isError = authCodeErrorRes != null,
                        supportingText = {
                            if (authCodeErrorRes != null) {
                                Text(
                                    text = stringResource(authCodeErrorRes),
                                    color = EmergencyRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.role_rescuer_demo_hint),
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onConfirmRole()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RescuerBadgeBlue,
                            unfocusedBorderColor = ColorSurfaceBorder,
                            focusedContainerColor = ColorSurfaceElevatedDark,
                            unfocusedContainerColor = ColorSurfaceDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            errorBorderColor = EmergencyRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Button(
                onClick = onConfirmRole,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRole == UserRole.RESCUER) RescuerBadgeBlue else EmergencyRed,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (selectedRole == UserRole.RESCUER) {
                            stringResource(R.string.role_confirm_rescuer)
                        } else {
                            stringResource(R.string.role_confirm_survivor)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    role: UserRole,
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ColorSurfaceElevatedDark else ColorSurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) badgeColor else ColorSurfaceBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton) { onClick() }
            .semantics {
                contentDescription = "$title. $description. ${if (isSelected) "Selected" else "Not selected"}."
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else ColorSurfaceElevatedDark)
                    .border(1.dp, if (isSelected) badgeColor else ColorSurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) badgeColor else TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) badgeColor else TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenSlide1Preview() {
    Surface(color = ColorBackgroundDark) {
        OnboardingScreen(
            uiState = OnboardingUiState(currentSlideIndex = 0, isShowingRoleSelection = false),
            onNextClicked = {},
            onSkipClicked = {},
            onSlideChanged = {},
            onSelectRole = {},
            onRescuerAuthCodeChanged = {},
            onConfirmRole = {}
        )
    }
}

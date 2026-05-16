package com.mindmatrix.budakattusante.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindmatrix.budakattusante.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Requirement 17: Onboarding/Tutorial.
 * High-quality welcome experience for first-time users.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            "Pure Forest Produce",
            "Direct from the tribes of Western Ghats to your kitchen. 100% organic and ethically harvested.",
            Icons.Default.Eco
        ),
        OnboardingPage(
            "Fair Trade for All",
            "We protect tribal livelihoods by enforcing Minimum Support Price (MSP) and giving direct share to artisans.",
            Icons.Default.Handshake
        ),
        OnboardingPage(
            "Trace Your Source",
            "Every product has a story. Scan the batch QR code to see which family harvested it and the exact forest zone.",
            Icons.Default.LocationOn
        )
    )

    Scaffold(
        containerColor = SandBeige
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { index ->
                OnboardingPageView(pages[index])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == index) ForestGreen else Color.LightGray)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp).width(140.dp)
                ) {
                    Text(
                        if (pagerState.currentPage == 2) "GET STARTED" else "NEXT",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

data class OnboardingPage(val title: String, val desc: String, val icon: ImageVector)

@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            color = ForestGreen.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = page.icon, 
                contentDescription = null, 
                modifier = Modifier.padding(48.dp),
                tint = ForestGreen
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            page.title,
            style = MaterialTheme.typography.headlineLarge,
            color = ForestGreen,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            page.desc,
            style = MaterialTheme.typography.bodyLarge,
            color = EarthBrown,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

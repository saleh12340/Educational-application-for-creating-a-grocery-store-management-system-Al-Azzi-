package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.lessons.LessonsCatalog
import com.example.ui.components.AlAzziTopBar
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardPrimary
import com.example.ui.theme.DarkPlayerSurface
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyInfo
import com.example.ui.theme.TextMutedOnDark
import com.example.ui.theme.TextOnDark
import com.example.util.ThermalPrinterHelper

@Composable
fun GitHubWorkflowScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val workflowText = LessonsCatalog.githubWorkflowYaml

    Surface(
        color = PureWhite,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            AlAzziTopBar(
                title = stringResource(R.string.github_screen_title),
                subtitle = ".github/workflows/build.yml • Free Tier",
                onBackClick = onBack
            )

            // Top-pinned Copy / Share Actions
            Surface(
                color = DarkCardPrimary,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ThermalPrinterHelper.copyToClipboard(
                                context = context,
                                label = "build.yml",
                                text = workflowText
                            )
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("copy_workflow_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("نسخ build.yml")
                    }

                    OutlinedButton(
                        onClick = {
                            ThermalPrinterHelper.shareReceiptText(context, workflowText)
                        },
                        border = BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("مشاركة الملف", color = TextOnDark)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = DarkCardPrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = EmeraldBright)
                            Text(
                                text = "خطوات البناء السحابي المجاني عبر GitHub Actions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextOnDark
                            )
                        }
                        val steps = listOf(
                            "1. أنشئ مجلد .github/workflows في جذر المستودع وضع بداخله ملف build.yml.",
                            "2. ادفع التعديلات من تطبيق Acode أو Termux إلى فرع main.",
                            "3. افتح تبويب Actions في مستودعك على GitHub (الباقة المجانية تمنحك 2000 دقيقة شهرياً).",
                            "4. بعد انتهاء البناء، حمّل ملف Al-Azzi-POS-Guide-Debug-APK وثبّته على هاتفك مباشرة."
                        )
                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedOnDark
                            )
                        }
                    }
                }

                Surface(
                    color = DarkCardPrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ".github/workflows/build.yml",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 13.sp,
                            color = SkyInfo
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkPlayerSurface, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = workflowText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = EmeraldBright
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.openLink

@Composable
fun OnboardingAgreementFooter() {
    val context = LocalContext.current

    val byContinuing = "By continuing you agree to our "
    val termsOfServices = "Terms of Services"
    val and = " and "
    val privacyPolicy = "Privacy Policy"

    val highlightStyle = SpanStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = interFontFamily,
        fontSize = 14.sp,
        color = Color(0xFF21A038)
    )

    val normalStyle = SpanStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = interFontFamily,
        fontSize = 14.sp,
        color = Color(0xFF545969)
    )

    val annotatedString = buildAnnotatedString {
        withStyle(style = normalStyle) {
            append(byContinuing)
        }
        withStyle(
            style = highlightStyle,
        ) {
            append(termsOfServices)
            addStringAnnotation(
                tag = "Terms",
                annotation = termsOfServices,
                start = byContinuing.length - 1,
                end = byContinuing.length + termsOfServices.length
            )
        }
        withStyle(style = normalStyle) {
            append(and)
        }
        withStyle(style = highlightStyle) {
            append(privacyPolicy)
            addStringAnnotation(
                tag = "Privacy",
                annotation = privacyPolicy,
                start = (byContinuing.length + termsOfServices.length + and.length) - 1,
                end = (byContinuing.length + termsOfServices.length + and.length + privacyPolicy.length)
            )
        }
        withStyle(style = normalStyle) {
            append(".")
        }
    }


    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    "Terms" -> {
                        context.openLink("https://orcus.com.bd/tos/")
                    }
                    "Privacy" -> {
                        context.openLink("https://orcus.com.bd/privacy-policy/")
                    }
                    else -> Unit
                }
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp),
        style = TextStyle(
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    )
}
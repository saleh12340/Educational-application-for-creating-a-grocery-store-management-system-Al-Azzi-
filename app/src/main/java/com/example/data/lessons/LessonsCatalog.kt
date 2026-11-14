package com.example.data.lessons

import com.example.data.model.LessonItem

object LessonsCatalog {

    val githubWorkflowYaml: String = """
name: Build Al-Azzi POS & Guide Android APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-apk:
    name: Build Debug APK (Free Tier)
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 21 (Temurin)
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Setup Gradle 9.3.1 & Cache
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'

      - name: Restore debug.keystore for debugConfig signing
        run: |
          if [ ! -f "debug.keystore" ]; then
            if [ -f "debug.keystore.base64" ]; then
              base64 -d debug.keystore.base64 > debug.keystore
            else
              keytool -genkeypair -v -keystore debug.keystore \
                -storepass android -alias androiddebugkey -keypass android \
                -keyalg RSA -keysize 2048 -validity 10000 \
                -dname "CN=Android Debug,O=Android,C=US"
            fi
          fi

      - name: Build Debug APK
        run: |
          if [ -f "./gradlew" ]; then
            chmod +x ./gradlew
            ./gradlew assembleDebug --no-daemon --stacktrace
          else
            gradle assembleDebug --no-daemon --stacktrace
          fi

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Al-Azzi-POS-Guide-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: error
          retention-days: 30
    """.trimIndent()

    val lessons: List<LessonItem> = listOf(
        LessonItem(
            id = 1,
            title = "الدرس الأول: التعامل مع النسخة المجانية بالكامل من Google AI Studio وضبط الإعدادات",
            subtitle = "تهيئة بيئة التطوير السحابية مجاناً بدون بطاقة بنكية وكتابة البرومبت الهندسي الدقيق",
            durationLabel = "11:20",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            topics = listOf("AI Studio Free Tier", "تصدير ZIP", "ربط GitHub"),
            guideSteps = listOf(
                "افتح Google AI Studio Build بحساب Google المجاني واختر قالب تطبيق أندرويد أصلي.",
                "قسّم طلبك إلى متطلبات واضحة: نظام الحسابات المحلي (SQLite)، ألوان الخلفية البيضاء والبطاقات الداكنة، وموضع حقول الإدخال في الأعلى.",
                "استخدم التحديثات المتدرجة في المحادثة لإضافة المزايا خطوة بخطوة ضمن الحصة المجانية اليومية.",
                "صدّر المشروع مباشرة إلى مستودع GitHub مجاني أو حمّله بصيغة ZIP لمتابعة التعديل في Acode على الهاتف."
            ),
            fileLabel = "Prompt Template — AI Studio Free Tier",
            codeSnippet = """
# قالب توجيه هندسي مجاني لـ Google AI Studio:
1. نوع المشروع: تطبيق أندرويد أصلي بواجهة عربية RTL.
2. التخزين: قاعدة بيانات SQLite محلية (Room) بدون أي تكاليف سحابية.
3. التصميم: خلفية بيضاء #FFFFFF بدون شفافية + بطاقات داكنة #111827.
4. تجربة الهاتف: تثبيت حقول الإدخال في أعلى الشاشة أعلى القائمة.
5. الأتمتة: إنشاء ملف .github/workflows/build.yml لبناء APK مجاناً.
            """.trimIndent()
        ),
        LessonItem(
            id = 2,
            title = "الدرس الثاني: تصميم الهيكل الأساسي لمشروع الأندرويد وتحديد متطلبات Java 17 و SDK 35",
            subtitle = "إعداد ملفات Gradle والمستودع وهيكلة الحزم والمكتبات الأساسية بدقة",
            durationLabel = "14:05",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            topics = listOf("SDK 35/36", "Java 17", "Gradle KTS", "Media3"),
            guideSteps = listOf(
                "تأكد من ضبط compileSdk و targetSdk مع توافق Java 17 في إعدادات Gradle وبيئة البناء.",
                "افصل الكود إلى طبقات نظيفة: data (الكيانات و DAO وقاعدة البيانات) و ui (الشاشات والسمات).",
                "فعّل مكتبات Media3 ExoPlayer للتشغيل التفاعلي ومكتبة Room لقاعدة بيانات SQLite المحلية.",
                "استخدم applicationId فريد لكل مشروع مع الحفاظ على namespace منظم للمصادر."
            ),
            fileLabel = "app/build.gradle.kts",
            codeSnippet = """
android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alazzi.pos.guide"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.room:room-runtime:2.7.0")
}
            """.trimIndent()
        ),
        LessonItem(
            id = 3,
            title = "الدرس الثالث: بناء واجهات المستخدم المخصصة لإدارة المتاجر (البطاقات الداكنة والخلفيات البيضاء)",
            subtitle = "تطبيق نظام الألوان عالي التباين وتثبيت الإدخالات في أعلى الشاشة للهواتف",
            durationLabel = "13:45",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            topics = listOf("خلفية بيضاء", "بطاقات داكنة عاتمة", "حماية الكيبورد"),
            guideSteps = listOf(
                "اضبط خلفية جميع الشاشات على اللون الأبيض النقي #FFFFFF بدون أي شفافية زجاجية (No Glassmorphism).",
                "استخدم بطاقات داكنة عاتمة تماماً #111827 للنصوص والملخصات لتوفير أقصى وضوح تحت إضاءة البقالة.",
                "ضع حقول البحث وإضافة العميل وتسجيل العملية في أعلى الشاشة تماماً حتى لا تغطيها لوحة المفاتيح.",
                "اعرض الاسم الكامل للعميل دون قص أو نقاط (...)، واجعل مربع الرصيد رقمياً فقط لسرعة القراءة."
            ),
            fileLabel = "CustomerCardTheme.kt",
            codeSnippet = """
// بطاقة عميل داكنة عاتمة فوق خلفية بيضاء نقية
Surface(
    color = Color(0xFF111827), // عاتم 100% بدون شفافية
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth()
) {
    Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // الاسم الكامل بدون أي قص أو Ellipsis
        Text(
            text = customer.fullName,
            color = Color.White,
            softWrap = true,
            modifier = Modifier.weight(1f)
        )
        // مربع الرصيد: القيمة الرقمية فقط
        Box(
            modifier = Modifier
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = formatNumericOnly(customer.currentBalance))
        }
    }
}
            """.trimIndent()
        ),
        LessonItem(
            id = 4,
            title = "الدرس الرابع: إدارة قواعد البيانات المحلية (SQLite) لتخزين بيانات الزبائن والفواتير محلياً مجاناً",
            subtitle = "تصميم الجداول والعلاقات والحفظ الآمن دون الحاجة لخوادم أو اشتراكات شهرية",
            durationLabel = "16:10",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
            topics = listOf("SQLite", "Room DAO", "Cascade Delete", "تصدير JSON"),
            guideSteps = listOf(
                "أنشئ جدول customers لحفظ الاسم الكامل والهاتف وجدول transactions لحفظ الديون والسداد مع ForeignKey.",
                "فعّل onDelete = CASCADE لحذف عمليات العميل تلقائياً عند حذفه ومنع بقاء سجلات يتيمة.",
                "استخدم تدفقات Flow التفاعلية لتحديث الأرصدة والجداول فوراً بمجرد تسجيل أي عملية جديدة.",
                "أضف ميزة النسخ الاحتياطي عبر Storage Access Framework لتصدير البيانات بصيغة JSON مجاناً دون أذونات تخزين خطرة."
            ),
            fileLabel = "AlAzziDatabase.kt",
            codeSnippet = """
@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = CustomerEntity::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customerId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: TransactionType, // DEBT or PAYMENT
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
            """.trimIndent()
        ),
        LessonItem(
            id = 5,
            title = "الدرس الخامس: برمجة شاشة سجل العمليات وتصميم الجداول العريضة للرصيد التراكمي",
            subtitle = "حساب الرصيد التراكمي بدقة لكل صف وعرض الأعمدة الخمسة بعرض الشاشة الكامل",
            durationLabel = "15:00",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            topics = listOf("جدول عريض", "الرصيد التراكمي", "التاريخ والوقت"),
            guideSteps = listOf(
                "رتب عمليات العميل تصاعدياً حسب الوقت (timestamp ASC) لحساب الرصيد التراكمي من أول عملية حتى آخر عملية.",
                "أضف المبلغ في حالة البيع الآجل (DEBT) واطرحه في حالة السداد النقدي (PAYMENT).",
                "صمم صف ترويسة داكن عاتم بعرض الشاشة الكامل يضم الأعمدة: العملية، التاريخ، الوقت، المبلغ، التراكمي.",
                "اعرض أحدث العمليات في الأعلى مباشرة أسفل نموذج الإدخال لتسهيل المراجعة الفورية على الكاشير."
            ),
            fileLabel = "CumulativeBalanceCalculator.kt",
            codeSnippet = """
fun computeLedgerRows(transactionsAsc: List<TransactionEntity>): List<LedgerRow> {
    var runningBalance = 0.0
    val rows = transactionsAsc.map { tx ->
        runningBalance = when (tx.type) {
            TransactionType.DEBT -> runningBalance + tx.amount
            TransactionType.PAYMENT -> runningBalance - tx.amount
        }
        LedgerRow(
            id = tx.id,
            customerId = tx.customerId,
            type = tx.type,
            amount = tx.amount,
            description = tx.description,
            timestamp = tx.timestamp,
            dateLabel = formatDate(tx.timestamp),
            timeLabel = formatTime(tx.timestamp),
            cumulativeBalance = runningBalance
        )
    }
    return rows.reversed() // الأحدث في الأعلى مع دقة التراكمي
}
            """.trimIndent()
        ),
        LessonItem(
            id = 6,
            title = "الدرس السادس: تطبيق تفاعلات اللمس المتقدمة (Long-press وتوجيهات التنقل العلوي)",
            subtitle = "برمجة الضغط المطول لخيارات التعديل والحذف وسهم الرجوع العلوي بدون شريط سفلي",
            durationLabel = "12:30",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            topics = listOf("Long-press", "HapticFeedback", "سهم الرجوع العلوي"),
            guideSteps = listOf(
                "استبدل أزرار التعديل والحذف المزدحمة في صفوف الجدول بتفاعل الضغط المطول combinedClickable.",
                "أضف اهتزازاً لمسياً خفيفاً (HapticFeedbackType.LongPress) لإعطاء المستخدم إحساساً فورياً بفتح قائمة الخيارات.",
                "ألغِ شريط التنقل السفلي (زر الرئيسية السفلي) تماماً لتوسيع مساحة القراءة ومنع اللمس بالخطأ.",
                "ضع سهم الرجوع في أعلى الشاشة في الشريط العلوي مع ربط زر الرجوع في النظام بنفس المسار."
            ),
            fileLabel = "LongPressRowModifier.kt",
            codeSnippet = """
Row(
    modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onSelectRow(row) },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedForAction = row
            }
        )
        .padding(vertical = 10.dp, horizontal = 8.dp)
) {
    // أعمدة الجدول العريض الخمسة
}
            """.trimIndent()
        ),
        LessonItem(
            id = 7,
            title = "الدرس السابع: ربط التطبيق بخيارات الطباعة الحرارية وفواتير الاستلام",
            subtitle = "توليد فاتورة حرارية متناسقة 58mm/80mm والطباعة عبر Android PrintManager أو البلوتوث",
            durationLabel = "14:50",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            topics = listOf("طباعة حرارية", "PrintManager", "مشاركة نصية"),
            guideSteps = listOf(
                "صمم نموذج فاتورة حرارية يحمل ترويسة (بقالة العزي للمواد الغذائية) واسم العميل الكامل والتاريخ والمبلغ والرصيد التراكمي.",
                "استخدم خدمة Android PrintManager مع مستند WebView HTML لطباعة الفاتورة أو حفظها بصيغة PDF بدون مكتبات مدفوعة.",
                "أضف خيار المشاركة النصية السريعة لتمرير الفاتورة إلى تطبيقات الطابعات الحرارية عبر البلوتوث أو واتساب.",
                "استخدم خط JetBrains Mono لمحاذاة الأرقام والأعمدة داخل الإيصال الحراري بوضوح تام."
            ),
            fileLabel = "ThermalReceiptPrinter.kt",
            codeSnippet = """
fun printThermalHtml(context: Context, jobName: String, htmlContent: String) {
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
}
            """.trimIndent()
        ),
        LessonItem(
            id = 8,
            title = "الدرس الثامن: إعداد وربط مستودع GitHub وتكوين نظام النسخة البرمجية المجاني",
            subtitle = "رفع ملفات المشروع من الهاتف عبر Acode و Git والاحتفاظ بسجل التغييرات مجاناً",
            durationLabel = "10:55",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            topics = listOf("GitHub Free", "Acode", "Git Push", ".gitignore"),
            guideSteps = listOf(
                "أنشئ مستودعاً جديداً مجانياً على GitHub (عام أو خاص — كلاهما يدعم GitHub Actions مجاناً).",
                "تأكد من وجود ملف .gitignore لاستبعاد مجلدات build و .gradle والملفات المحلية المؤقتة.",
                "افتح المشروع في تطبيق Acode أو Termux واربط المستودع بأمر git remote add origin.",
                "ادفع التعديلات إلى الفرع main ليبدأ خادم GitHub السحابي بتجميع التطبيق تلقائياً."
            ),
            fileLabel = "Git Setup Commands (Termux / Acode)",
            codeSnippet = """
git init
git add .
git commit -m "Initial commit: Al-Azzi POS & Guide App"
git branch -M main
git remote add origin https://github.com/USERNAME/al-azzi-pos-guide.git
git push -u origin main
            """.trimIndent()
        ),
        LessonItem(
            id = 9,
            title = "الدرس التاسع: بناء وتهيئة ملفات GitHub Actions الآلية لتجميع وضبط ملفات الـ APK مجاناً",
            subtitle = "كتابة ملف .github/workflows/build.yml لاستخراج ملف app-debug.apk جاهز للتحميل",
            durationLabel = "15:30",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
            topics = listOf("GitHub Actions", "Java 17", "assembleDebug", "Artifacts"),
            guideSteps = listOf(
                "أنشئ المسار .github/workflows/build.yml داخل جذر المستودع.",
                "حدد المشغّل ubuntu-latest المجاني الذي يمنحك 2000 دقيقة بناء شهرية مجاناً على GitHub.",
                "أضف خطوات setup-java@v4 بنسخة 17، ثم setup-android@v3، ثم setup-gradle@v4 لتسريع التحميل.",
                "شغّل ./gradlew assembleDebug وارفع ملف app/build/outputs/apk/debug/*.apk عبر upload-artifact@v4."
            ),
            fileLabel = ".github/workflows/build.yml",
            codeSnippet = githubWorkflowYaml
        ),
        LessonItem(
            id = 10,
            title = "الدرس العاشر: اختبار التطبيق نهائياً، اكتشاف الأخطاء، وحل مشاكل التوافقية على الهواتف",
            subtitle = "فحص شاشات الهاتف، تجنب تداخل الكيبورد، واختبارات Robolectric قبل التثبيت النهائي",
            durationLabel = "13:15",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            topics = listOf("اختبار الواجهة", "RTL", "ExoPlayer Lifecycle", "APK Verification"),
            guideSteps = listOf(
                "تحقق من تحرير موارد ExoPlayer داخل DisposableEffect(onDispose) لمنع استنزاف الذاكرة عند الرجوع.",
                "اختبر فتح لوحة المفاتيح على الشاشات الصغيرة للتأكد من بقاء حقول الإدخال العلوية ظاهرة بالكامل.",
                "تأكد من إعادة احتساب الرصيد التراكمي عند تعديل أو حذف أي عملية قديمة من وسط السجل.",
                "حمّل ملف APK من تبويب Actions في GitHub وثبّته على هاتفك للتحقق من سرعة الاستجابة والطباعة."
            ),
            fileLabel = "ExoPlayerLifecycleCheck.kt",
            codeSnippet = """
DisposableEffect(lesson.id) {
    val mediaItem = MediaItem.fromUri(lesson.videoUrl)
    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
    onDispose {
        exoPlayer.pause()
    }
}

DisposableEffect(Unit) {
    onDispose {
        exoPlayer.release() // تحرير الذاكرة تماماً عند مغادرة الشاشة
    }
}
            """.trimIndent()
        )
    )
}

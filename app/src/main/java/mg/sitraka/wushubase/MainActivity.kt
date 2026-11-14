@file:OptIn(ExperimentalMaterial3Api::class)

package mg.sitraka.wushubase

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.Normalizer

val LocalLang = compositionLocalOf { "mg" }

@Composable
fun tr(mg: String, fr: String): String = if (LocalLang.current == "mg") mg else fr

private fun norm(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wushu_prefs", MODE_PRIVATE)
        setContent { WushuApp(prefs) }
    }
}

private data class NavItem(val icon: ImageVector, val label: String)

@Composable
fun WushuApp(prefs: SharedPreferences) {
    var themeMode by remember { mutableIntStateOf(prefs.getInt("theme", 0)) }
    var lang by remember { mutableStateOf(prefs.getString("lang", "mg") ?: "mg") }
    var learned by remember { mutableStateOf(prefs.getStringSet("learned", emptySet())?.toSet() ?: emptySet()) }
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    CompositionLocalProvider(LocalLang provides lang) {
        WushuTheme(dark) {
            var tab by rememberSaveable { mutableIntStateOf(0) }
            var catId by rememberSaveable { mutableStateOf<String?>(null) }
            var techId by rememberSaveable { mutableStateOf<String?>(null) }

            BackHandler(enabled = techId != null || catId != null) {
                if (techId != null) techId = null else catId = null
            }

            val items = listOf(
                NavItem(Icons.Filled.Home, tr("Teknika", "Techniques")),
                NavItem(Icons.AutoMirrored.Filled.List, tr("Voambolana", "Vocabulaire")),
                NavItem(Icons.Filled.Info, tr("Tantara", "Histoire")),
                NavItem(Icons.Filled.Settings, tr("Fanamboarana", "Paramètres")),
            )

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { idx, item ->
                            NavigationBarItem(
                                selected = tab == idx,
                                onClick = { tab = idx; catId = null; techId = null },
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.label, maxLines = 1) }
                            )
                        }
                    }
                }
            ) { pad ->
                Box(Modifier.padding(pad).fillMaxSize()) {
                    val tech = TECHS.firstOrNull { it.id == techId }
                    val cat = CATS.firstOrNull { it.id == catId }
                    when {
                        tech != null && tab == 0 -> TechDetail(
                            tech = tech,
                            learned = tech.id in learned,
                            onToggle = {
                                learned = if (tech.id in learned) learned - tech.id else learned + tech.id
                                prefs.edit().putStringSet("learned", learned.toSet()).apply()
                            },
                            onBack = { techId = null }
                        )
                        tab == 0 && cat != null -> CategoryScreen(cat, learned, onBack = { catId = null }, onTech = { techId = it })
                        tab == 0 -> HomeScreen(learned, onCat = { catId = it }, onTech = { catId = it.cat; techId = it.id })
                        tab == 1 -> VocabScreen()
                        tab == 2 -> HistoryScreen()
                        else -> SettingsScreen(
                            themeMode = themeMode, lang = lang,
                            onTheme = { themeMode = it; prefs.edit().putInt("theme", it).apply() },
                            onLang = { lang = it; prefs.edit().putString("lang", it).apply() }
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────── Accueil ─────────────────────────

@Composable
fun HomeScreen(learned: Set<String>, onCat: (String) -> Unit, onTech: (Tech) -> Unit) {
    val lang = LocalLang.current
    var q by rememberSaveable { mutableStateOf("") }
    val results = remember(q, lang) {
        val nq = norm(q.trim())
        if (nq.isBlank()) emptyList()
        else TECHS.filter {
            norm(it.hanzi + " " + it.pinyin + " " + it.fr + " " + it.mg).contains(nq)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Hero(learned.count { id -> TECHS.any { it.id == id } }, TECHS.size)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = q, onValueChange = { q = it },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, null) }
                },
                placeholder = { Text(tr("Hikaroka teknika… (拳, quan, kick)", "Chercher une technique… (拳, quan, coup de pied)")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (q.isBlank()) {
            items(CATS) { c ->
                val idx = CATS.indexOf(c)
                CatCard(c, TECHS.count { it.cat == c.id }, idx % 2 == 0) { onCat(c.id) }
            }
        } else {
            if (results.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(tr("Tsy nahitana na inona na inona.", "Aucun résultat."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(results, span = { GridItemSpan(maxLineSpan) }) { t ->
                TechRow(t, t.id in learned) { onTech(t) }
            }
        }
    }
}

@Composable
private fun Hero(done: Int, total: Int) {
    val gold = Color(0xFFFFE082)
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF7A1212), Color(0xFFC62828), Color(0xFFE59A00))))
            .padding(22.dp)
    ) {
        Column {
            Text("武术", fontSize = 46.sp, fontWeight = FontWeight.Black, color = gold)
            Text("Wǔshù · Wushu Base", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                tr("Mianara ny fototry ny wushu : teknika, toerana, fitsambikinana, tantara ary voambolana.",
                    "Apprenez les bases du wushu : techniques, positions, sauts, histoire et vocabulaire."),
                color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp
            )
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = gold, trackColor = Color(0x55FFFFFF)
            )
            Spacer(Modifier.height(6.dp))
            Text(tr("$done / $total efa nianarina", "$done / $total appris"), color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CatCard(c: Cat, count: Int, red: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val lang = LocalLang.current
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (red) cs.primaryContainer else cs.secondaryContainer),
        modifier = Modifier.fillMaxWidth().heightIn(min = 168.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(c.hanzi, fontSize = if (c.hanzi.length > 3) 24.sp else 32.sp, fontWeight = FontWeight.Bold)
            Text(c.pinyin, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (red) cs.primary else cs.onSecondaryContainer)
            Text(if (lang == "mg") c.mg else c.fr, fontSize = 12.sp, maxLines = 4, lineHeight = 16.sp)
            Spacer(Modifier.weight(1f, fill = false))
            Text("$count ${tr("teknika", "techniques")}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CategoryScreen(cat: Cat, learned: Set<String>, onBack: () -> Unit, onTech: (String) -> Unit) {
    val lang = LocalLang.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Column {
                    Text(cat.hanzi, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(cat.pinyin, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(if (lang == "mg") cat.mg else cat.fr, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(TECHS.filter { it.cat == cat.id }) { t ->
            TechRow(t, t.id in learned) { onTech(t.id) }
        }
    }
}

@Composable
private fun TechRow(t: Tech, learned: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val lang = LocalLang.current
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t.hanzi, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(t.pinyin, fontSize = 15.sp, color = cs.primary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (lang == "mg") t.mg else t.fr,
                    fontSize = 13.sp, maxLines = 2, color = cs.onSurfaceVariant, lineHeight = 17.sp
                )
            }
            if (learned) {
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(cs.secondary),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Check, null, tint = cs.onSecondary, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

// ───────────────────────── Détail ─────────────────────────

@Composable
fun TechDetail(tech: Tech, learned: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val lang = LocalLang.current
    val cat = CATS.first { it.id == tech.cat }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("${cat.hanzi} · ${cat.pinyin}", color = cs.primary, fontWeight = FontWeight.SemiBold)
        }
        FigureStage(tech.poses, Modifier.fillMaxWidth().height(280.dp))
        Column {
            Text(tech.hanzi, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text(tech.pinyin, fontSize = 22.sp, color = cs.primary, fontWeight = FontWeight.Medium)
        }
        InfoCard(tr("Famaritana", "Description"), if (lang == "mg") tech.mg else tech.fr, cs.surfaceVariant)
        InfoCard(tr("Teboka fototra", "Point clé"), if (lang == "mg") tech.keyMg else tech.keyFr, cs.secondaryContainer)
        Text(
            tr("Sary mihetsika fanehoana ny fihetsika ihany. Jereo foana ny mpampianatra mba hahitana ny endrika marina.",
                "Animation illustrative. Référez-vous toujours à votre maître pour la forme exacte."),
            fontSize = 12.sp, color = cs.onSurfaceVariant
        )
        if (learned) {
            Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(tr("Efa nianarina", "Appris"))
            }
        } else {
            OutlinedButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Text(tr("Mariho ho efa nianarina", "Marquer comme appris"))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String, container: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text, fontSize = 16.sp, lineHeight = 22.sp)
        }
    }
}

// ───────────────────────── Vocabulaire ─────────────────────────

@Composable
fun VocabScreen() {
    val cs = MaterialTheme.colorScheme
    val lang = LocalLang.current
    var q by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    val list = remember(q, group, lang) {
        val nq = norm(q.trim())
        WORDS.filter { w ->
            (group == null || w.group == group) &&
                (nq.isBlank() || norm(w.hanzi + " " + w.pinyin + " " + w.fr + " " + w.mg).contains(nq))
        }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text("词汇", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("cíhuì · " + tr("Voambolana ilaina amin'ny fanazarana", "Vocabulaire d'entraînement"), color = cs.primary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = q, onValueChange = { q = it },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, null) } },
                placeholder = { Text(tr("Hikaroka…", "Rechercher…")) },
                singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(selected = group == null, onClick = { group = null }, label = { Text(tr("Rehetra", "Tous")) })
            }
            items(GROUPS) { g ->
                FilterChip(selected = group == g.id, onClick = { group = g.id }, label = { Text("${g.hanzi} ${g.pinyin}") })
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list) { w ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(w.hanzi, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 88.dp, max = 120.dp))
                        Column(Modifier.weight(1f)) {
                            Text(w.pinyin, color = cs.primary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Text(if (lang == "mg") w.mg else w.fr, fontSize = 14.sp, color = cs.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Histoire & philosophie ─────────────────────────

@Composable
fun HistoryScreen() {
    val cs = MaterialTheme.colorScheme
    var section by rememberSaveable { mutableIntStateOf(0) }
    val data = if (section == 0) HISTORY else PHILO
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text("历史 · 哲学", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("lìshǐ · zhéxué", color = cs.primary, fontWeight = FontWeight.Medium)
        }
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = section == 0, onClick = { section = 0 }, label = { Text("历史 · " + tr("Tantara", "Histoire")) })
            FilterChip(selected = section == 1, onClick = { section = 1 }, label = { Text("哲学 · " + tr("Fisainana", "Philosophie")) })
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(data, key = { "$section-${it.pinyin}" }) { a -> ArticleCard(a) }
        }
    }
}

@Composable
private fun ArticleCard(a: Article) {
    val cs = MaterialTheme.colorScheme
    val lang = LocalLang.current
    var open by rememberSaveable { mutableStateOf(false) }
    Card(
        onClick = { open = !open },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (open) cs.secondaryContainer else cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(a.hanzi, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(a.pinyin, color = cs.primary, fontWeight = FontWeight.Medium)
                    Text(if (lang == "mg") a.tMg else a.tFr, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(if (open) "▴" else "▾", fontSize = 20.sp)
            }
            if (open) {
                Spacer(Modifier.height(10.dp))
                Text(if (lang == "mg") a.bMg else a.bFr, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}

// ───────────────────────── Paramètres ─────────────────────────

@Composable
fun SettingsScreen(themeMode: Int, lang: String, onTheme: (Int) -> Unit, onLang: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("设置", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("shèzhì · " + tr("Fanamboarana", "Paramètres"), color = cs.primary, fontWeight = FontWeight.Medium)

        SettingCard(tr("Endrika (theme)", "Thème")) {
            RadioRow(themeMode == 0, tr("Araka ny finday", "Selon le système")) { onTheme(0) }
            RadioRow(themeMode == 1, tr("Mazava (clair)", "Clair")) { onTheme(1) }
            RadioRow(themeMode == 2, tr("Maizina (dark)", "Sombre")) { onTheme(2) }
        }
        SettingCard(tr("Fiteny", "Langue")) {
            RadioRow(lang == "mg", "Malagasy") { onLang("mg") }
            RadioRow(lang == "fr", "Français") { onLang("fr") }
            Text(
                tr("Ny anaran'ny teknika sy ny teny ara-teknika rehetra dia mijanona amin'ny teny sinoa (pinyin + caractère).",
                    "Les noms de techniques et tous les termes techniques restent en chinois (pinyin + caractères)."),
                fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)
            )
        }
        SettingCard(tr("Momban'ny application", "À propos")) {
            Text("武术 Wushu Base  ·  v1.0.0", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(tr("Mpamorona : Sitraka Nambinintsoa", "Créateur : Sitraka Nambinintsoa"))
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun RadioRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 16.sp)
    }
}

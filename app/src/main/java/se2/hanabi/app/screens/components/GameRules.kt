import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HanabiRulesScreen() : Unit {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionTitle("Hanabi – Spielregeln")
        Text("Kooperatives Kartenspiel für 2–5 Spieler")

        Section("Spielübersicht") {
            BulletPoint("Spielart: Kooperatives Kartenspiel")
            BulletPoint("Spieleranzahl: 2–5 Spieler")
            BulletPoint("Spielleiter: 1 Spieler übernimmt die Verwaltung der Plättchen")
        }

        Section("Spielvorbereitung") {
            BulletPoint("Hinweissteine: 8 Stück, mit weißer Seite nach oben")
            BulletPoint("Gewitterplättchen: 3 Stück, mit klarer Seite nach oben")
            BulletPoint("Kartenausgabe:")
            IndentedBulletPoint("2–3 Spieler: Je 5 Karten")
            IndentedBulletPoint("4–5 Spieler: Je 4 Karten")
        }

        Section("Grundregeln") {
            BulletPoint("Jeder Spieler sieht nur die Karten der Mitspieler")
            BulletPoint("Gespielt wird im Uhrzeigersinn")
            BulletPoint("Pro Zug: genau eine von drei Aktionen")
        }

        Section("Spielaktionen") {
            Subsection("1. Hinweis geben") {
                BulletPoint("Hinweis über Anzahl & Position einer Zahl oder Farbe")
                BulletPoint("Beispiel: „Drei Einsen an Position 1, 3 und 5.“")
                BulletPoint("Nicht möglich, wenn alle Hinweissteine schwarz sind")
            }

            Subsection("2. Karte abwerfen") {
                BulletPoint("Nur erlaubt, wenn ein Hinweisstein schwarz ist")
                BulletPoint("Hinweisstein wird wieder weiß")
                BulletPoint("Neue Karte vom Nachziehstapel ziehen")
            }

            Subsection("3. Karte ausspielen") {
                BulletPoint("Karten in Reihenfolge 1–5, farbsortiert auslegen")
                BulletPoint("Falsche Karte → Gewitterplättchen wird umgedreht")
                BulletPoint("Bei 3 umgedrehten Gewitterplättchen: Spiel verloren")
                BulletPoint("Keine doppelte Farbe erlaubt")
                BulletPoint("Vollständige Reihe (1–5) → Hinweisstein zurück auf weiß")
            }
        }

        Section("Spielende") {
            BulletPoint("Drei Gewitterplättchen umgedreht → Spiel verloren")
            BulletPoint("Letzte Karte gezogen → jeder noch 1 Zug")
            BulletPoint("Alle Farbreihen (1–5) vollständig → 25 Punkte & Sieg")
            BulletPoint("Wertung: höchste Karte pro Farbe zählt")
        }
    }
}
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Column { content() }
}

@Composable
fun Subsection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Column { content() }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", style = MaterialTheme.typography.bodyLarge)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun IndentedBulletPoint(text: String) {
    Row(modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)) {
        Text("◦ ", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
@Preview
@Composable
fun HanabiRulesScreenPreview() {
    HanabiRulesScreen()
}


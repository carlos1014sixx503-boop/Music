package com.example.data.lyrics

import com.example.data.Song

object LyricsRepository {

    /**
     * Parsea texto en formato LRC estándar ([mm:ss.xx] texto) a lista estructurada de LyricLine.
     */
    fun parseLrc(lrcText: String): List<LyricLine> {
        val pattern = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\](.*)")
        val lines = mutableListOf<LyricLine>()

        lrcText.lines().forEach { rawLine ->
            val match = pattern.find(rawLine.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val millisPart = match.groupValues[3]
                val millis = when (millisPart.length) {
                    2 -> (millisPart.toLongOrNull() ?: 0L) * 10
                    3 -> millisPart.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = (min * 60 + sec) * 1000L + millis
                val lyricText = match.groupValues[4].trim()
                if (lyricText.isNotBlank()) {
                    lines.add(LyricLine(timestampMs = totalMs, text = lyricText))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    private val sampleLyricsDatabase = mapOf(
        "song_1" to listOf(
            LyricLine(0L, "♪ (Sintetizadores análogos 503 iluminan el valle) ♪"),
            LyricLine(8000L, "Las luces de San Salvador empiezan a titilar"),
            LyricLine(16000L, "Desde el mirador del volcán se puede contemplar"),
            LyricLine(24000L, "El neón recorre la arteria de la capital"),
            LyricLine(32000L, "Un bajo sintético que nunca va a parar"),
            LyricLine(42000L, "Distrito 503, la noche cobra vida"),
            LyricLine(50000L, "Bajo la brisa fresca de la cordillera encendida"),
            LyricLine(62000L, "Caminando entre memorias de asfalto y cristal"),
            LyricLine(74000L, "Este es el pulso urbano, puro y digital"),
            LyricLine(88000L, "♪ (Solo de sintetizador analógico Moog) ♪"),
            LyricLine(105000L, "Las calles vacías susurran una melodía"),
            LyricLine(118000L, "Que se transforma en fuego al llegar el nuevo día"),
            LyricLine(132000L, "Distrito 503 en el dial de tu corazón"),
            LyricLine(148000L, "Donde la patria suena con fuerza y pasión"),
            LyricLine(165000L, "Brillando en alta fidelidad y resolución"),
            LyricLine(185000L, "♪ (Fade-out atmosférico sobre el Boquerón) ♪")
        ),
        "song_2" to listOf(
            LyricLine(0L, "♪ (Ritmo dembow y percusión acústica salvadoreña) ♪"),
            LyricLine(6000L, "Directo desde la Calle Real con sabor"),
            LyricLine(12000L, "Traemos la vibra que quita el dolor"),
            LyricLine(19000L, "Siente la tarima, la gente bailando al compás"),
            LyricLine(28000L, "Aquí en El Salvador no miramos hacia atrás"),
            LyricLine(38000L, "Ritmo en las esquinas, gente trabajadora"),
            LyricLine(48000L, "Esta es la rumba que la noche entera enamora"),
            LyricLine(58000L, "Carlos Martínez soltando la rima certera"),
            LyricLine(70000L, "Uniendo a toda la costa y la frontera"),
            LyricLine(85000L, "Subile al volumen, siente el golpe del bajo 503"),
            LyricLine(98000L, "Esto no se compara con nada de lo que antes oyó usted"),
            LyricLine(115000L, "¡Distrito Music sonando en cada rincón!"),
            LyricLine(130000L, "Orgullo de barrio, raíz y tradición"),
            LyricLine(150000L, "♪ (Break de percusión con redobles latinos) ♪"),
            LyricLine(170000L, "Nos vemos en la pista cuando vuelva a caer el sol")
        ),
        "song_3" to listOf(
            LyricLine(0L, "♪ (Paisaje sonoro espacial y ondas binaurales) ♪"),
            LyricLine(15000L, "Flotando sobre órbitas de añil y plata"),
            LyricLine(32000L, "Una nebulosa que en el espacio se desata"),
            LyricLine(50000L, "Las estrellas resuenan en clave de Sol"),
            LyricLine(70000L, "Conectando constelaciones desde El Salvador"),
            LyricLine(95000L, "Galaxia 503... silencio en gravedad cero"),
            LyricLine(120000L, "Un viaje infinito donde el sonido es sincero"),
            LyricLine(145000L, "Armónicos cristalinos viajando por el éter"),
            LyricLine(175000L, "Donde el alma descansa y no busca volver"),
            LyricLine(210000L, "♪ (Eco cósmico en 3D surround sound) ♪"),
            LyricLine(240000L, "Frecuencias celestiales en paz infinita")
        ),
        "song_4" to listOf(
            LyricLine(0L, "♪ (Arpegios brillantes de sintetizador retro) ♪"),
            LyricLine(10000L, "Reflejos de la urbe en cristales de lluvia"),
            LyricLine(22000L, "La memoria distante de una noche que huye"),
            LyricLine(35000L, "Sueños de cristal que no se van a romper"),
            LyricLine(50000L, "Porque nacieron con ganas de vencer"),
            LyricLine(68000L, "Luces del volcán guardando nuestra historia"),
            LyricLine(85000L, "Grabadas en pistas para la memoria"),
            LyricLine(105000L, "Distrito 503 sintetiza el porvenir"),
            LyricLine(125000L, "La música que nos enseña a vivir"),
            LyricLine(150000L, "♪ (Clímax rítmico electro-wave) ♪"),
            LyricLine(180000L, "Cristal y neón, brillando hasta el fin")
        ),
        "song_5" to listOf(
            LyricLine(0L, "♪ (Guitarras tropicales y olas del mar Pacifíco) ♪"),
            LyricLine(12000L, "La brisa en la Costa del Sol acaricia la piel"),
            LyricLine(25000L, "Un coco frío, la tarde dulce como miel"),
            LyricLine(40000L, "Bajo el cielo añil que baña este litoral"),
            LyricLine(56000L, "Nuestra tierra querida tiene un brillo especial"),
            LyricLine(72000L, "Sonsonate en el pulso, ritmo para gozar"),
            LyricLine(90000L, "Deja que la marea te enseñe a soñar"),
            LyricLine(115000L, "Distrito 503 lleva la vibra marina"),
            LyricLine(140000L, "A cada bocina y a cada esquina"),
            LyricLine(170000L, "♪ (Vientos y metales cálidos tropicales) ♪"),
            LyricLine(200000L, "Atardecer dorado en las olas de El Zonte")
        ),
        "song_6" to listOf(
            LyricLine(0L, "♪ (Bassline 808 profundo y compás urbano) ♪"),
            LyricLine(10000L, "Frecuencia calibrada al pulso del peatón"),
            LyricLine(24000L, "Letras con sentido que tocan el corazón"),
            LyricLine(40000L, "No es solo rima, es testimonio y sudor"),
            LyricLine(58000L, "De quienes construyen un país mejor"),
            LyricLine(76000L, "Subiendo la pendiente con la frente en alto"),
            LyricLine(95000L, "Nuestra música no se queda en el asfalto"),
            LyricLine(118000L, "Suena en el bus, suena en el taller"),
            LyricLine(138000L, "Frecuencia Urbana para amanecer"),
            LyricLine(155000L, "Carlos Martínez en la voz del pueblo")
        ),
        "song_7" to listOf(
            LyricLine(0L, "♪ (Pads etéreos y cuerdas ambientales) ♪"),
            LyricLine(18000L, "Ecos lejanos en la inmensidad estelar"),
            LyricLine(42000L, "Ondas que flotan sin necesidad de hablar"),
            LyricLine(70000L, "Nebulosa de sueños, templo de meditación"),
            LyricLine(105000L, "Donde la mente encuentra su propia canción"),
            LyricLine(145000L, "La vibración 503 expandiéndose en el cosmos"),
            LyricLine(190000L, "Serenidad pura para el alma y los ojos"),
            LyricLine(240000L, "Paz sonora en alta definición")
        )
    )

    fun getLyricsForSong(song: Song): SongLyrics {
        val specific = sampleLyricsDatabase[song.id]
        if (specific != null) {
            return SongLyrics(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                lines = specific,
                isSynchronized = true,
                source = "Distrito 503 Master Lyrics"
            )
        }

        // Generar letras rítmicas sincronizadas por defecto basadas en la duración real de la pista
        val durationMs = (song.durationSeconds * 1000L).coerceAtLeast(60000L)
        val stepMs = (durationMs / 8).coerceAtLeast(10000L)

        val generatedLines = listOf(
            LyricLine(0L, "♪ (${song.genre} en reproducción Hi-Res) ♪"),
            LyricLine(stepMs, "Siente la vibración de ${song.title}"),
            LyricLine(stepMs * 2, "La maestría sonora de ${song.artist}"),
            LyricLine(stepMs * 3, "Resonando con pureza en ${song.album}"),
            LyricLine(stepMs * 4, "Ecualización calibrada para alta fidelidad"),
            LyricLine(stepMs * 5, "Distrito Music 503 • Identidad y calidad musical"),
            LyricLine(stepMs * 6, "Cada frecuencia revela un nuevo detalle"),
            LyricLine(stepMs * 7, "Música salvadoreña que trasciende fronteras")
        )

        return SongLyrics(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            lines = generatedLines,
            isSynchronized = true,
            source = "Distrito 503 Engine"
        )
    }
}

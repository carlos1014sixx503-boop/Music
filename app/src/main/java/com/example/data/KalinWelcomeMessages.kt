package com.example.data

/**
 * Representa un mensaje de bienvenida de la personalidad "Kalin".
 */
data class KalinMessage(
    val id: String,
    val message: String,
    val author: String = "Kalin",
    val iconEmoji: String = "🎵"
)

/**
 * Proveedor de mensajes de bienvenida de Kalin.
 * Diseñado de forma modular para permitir agregar más mensajes fácilmente
 * en el futuro sin modificar la lógica del reproductor ni la interfaz de usuario.
 */
object KalinMessageProvider {

    // Lista inicial de 5 mensajes configurados
    private val defaultMessages: List<KalinMessage> = listOf(
        KalinMessage(
            id = "kalin_msg_1",
            message = "Bienvenido a tu app de música. Kalin te desea un excelente día. 🎵",
            iconEmoji = "🎵"
        ),
        KalinMessage(
            id = "kalin_msg_2",
            message = "Me alegra que vuelvas a tu mejor opción para escuchar música. Que tengas un excelente día y nunca te desanimes. —Kalin 🎶",
            iconEmoji = "🎶"
        ),
        KalinMessage(
            id = "kalin_msg_3",
            message = "¡Hola! Kalin está listo para acompañarte con tus canciones favoritas. Disfruta tu música. 🎧",
            iconEmoji = "🎧"
        ),
        KalinMessage(
            id = "kalin_msg_4",
            message = "¡Qué bueno verte de nuevo! Pon tu canción favorita, relájate y disfruta el momento. —Kalin 🎵",
            iconEmoji = "🎵"
        ),
        KalinMessage(
            id = "kalin_msg_5",
            message = "Bienvenido nuevamente. La música está lista, solo falta que elijas qué quieres escuchar. ¡Disfruta! —Kalin 🎶",
            iconEmoji = "🎶"
        )
    )

    // Lista dinámica mutable que permite registrar más mensajes en el futuro
    private val messagesPool = defaultMessages.toMutableList()

    /**
     * Retorna todos los mensajes disponibles.
     */
    fun getAllMessages(): List<KalinMessage> = messagesPool.toList()

    /**
     * Permite agregar nuevos mensajes en el futuro sin reescribir lógica.
     */
    fun addMessage(message: KalinMessage) {
        messagesPool.add(message)
    }

    /**
     * Selecciona aleatoriamente uno de los mensajes disponibles.
     * Opcionalmente evita repetir el último mensaje mostrado.
     */
    fun getRandomMessage(lastMessageId: String? = null): KalinMessage {
        val candidates = if (messagesPool.size > 1 && lastMessageId != null) {
            messagesPool.filter { it.id != lastMessageId }
        } else {
            messagesPool
        }
        return candidates.random()
    }
}

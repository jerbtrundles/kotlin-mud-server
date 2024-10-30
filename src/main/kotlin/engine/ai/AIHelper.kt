package engine.ai

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.generativeai.GenerativeModel
import com.google.cloud.vertexai.generativeai.ResponseHandler

object AIHelper {
    val vertexAI = VertexAI("gen-lang-client-0672052936", "us-west1")
    val model = GenerativeModel("gemini-pro", vertexAI)

    private const val boilerplate = """Create a 5-line conversation between two random village characters
        (e.g., blacksmith, wizard, baker). Choose a random topic (e.g., weather, profession, shop feedback).
        Use the format: Name: "Dialogue."
        Output only the conversation, nothing else.
        Keep it under 300 characters."""

    fun request(requestString: String): String {
        while (true) {
            try {
                val response = ResponseHandler.getText(model.generateContent(boilerplate))
                if (response.isEmpty()) {
                    println("\tEMPTY RESULT.")
                } else {
                    println("\tRESPONSE: $response")
                    return response
                }
            } catch (e: Exception) {
                println("\tERROR: ${e.message}")
                Thread.sleep(1000)
            }
        }
    }
}
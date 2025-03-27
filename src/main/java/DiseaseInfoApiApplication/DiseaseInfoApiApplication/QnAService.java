package DiseaseInfoApiApplication.DiseaseInfoApiApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Map;

@Service
public class QnAService {

    private static final Logger logger = LoggerFactory.getLogger(QnAService.class);

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    private final String geminiApiKey = "AIzaSyAq_EuTPeHprPb2W9adTDW70EEvsj10h9A"; // Replace with your actual API key

    private final WebClient webClient;

    public QnAService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String diseaseName,String city) {
        logger.info("Getting answer for disease: {}", diseaseName);

        String prompt = "Provide information about " + diseaseName + " in" + city +" , with 10 hospitals details  in the following JSON format:\\n" +
                "{\\n" +
                "  \\\"hospitals\\\": [],\\n" +
                "\\\"Beds availibility\\\":\\\"give according to above hospitals\\\",\\n"+
                "  \\\"budget\\\": {\\n" +
                "    \\\"consultation\\\": \\\"Cost range\\\",\\n" +
                "    \\\"tests\\\": \\\"Cost range\\\",\\n" +
                "    \\\"medications\\\": \\\"Cost range\\\",\\n" +
                "    \\\"hospitalization\\\": \\\"Cost range\\\"\\n" +
                "  },\\n" +
                "  \\\"insurance_companies\\\": [],\\n" +
                "  \\\"cost\\\": \\\"Cost range\\\",\\n" +
                "  \\\"recovery_time\\\": \\\"Time range\\\"\\n" +
                "  \\\"precautions\\\" : [],\\n" +
                "}";

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            String response = webClient.post()
                    .uri(geminiApiUrl + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("Gemini API response: {}", response);
            return extractDiseaseInfo(response);
        } catch (WebClientResponseException e) {
            logger.error("Error calling Gemini API: {}", e.getResponseBodyAsString(), e);
            return "{\"error\": \"Gemini API error: " + e.getRawStatusCode() + " " + e.getResponseBodyAsString() + "\"}";
        } catch (Exception e) {
            logger.error("Unexpected error calling Gemini API", e);
            return "{\"error\": \"Unexpected error: " + e.getMessage() + "\"}";
        }
    }

    private String extractDiseaseInfo(String geminiApiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(geminiApiResponse);
            String jsonText = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();

            // Remove ```json and ```
            jsonText = jsonText.replace("```json", "").replace("```", "").trim();

            //validate json.
            objectMapper.readTree(jsonText);

            return jsonText;

        } catch (IOException e) {
            e.printStackTrace();
            return "{\"error\": \"Error parsing Gemini API response\"}";
        }
    }
}
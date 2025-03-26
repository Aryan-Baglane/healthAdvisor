package DiseaseInfoApiApplication.DiseaseInfoApiApplication;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
// this will change the code

@RestController
@AllArgsConstructor
@RequestMapping("/api/disease")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final QnAService qnaService;

    @PostMapping("/info")
    public ResponseEntity<String> getDiseaseInfoPost(@RequestBody Map<String, String> payload) {
        String diseaseName = payload.get("disease");
        String city = payload.get("city");
        if ((diseaseName == null) && (city == null) ) {
            return ResponseEntity.badRequest().body("{\"error\": \"Missing 'disease' parameter in request body\"}");
        }
        try {
            String answer = qnaService.getAnswer(diseaseName,city);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            logger.error("Error processing POST request for disease: {}", diseaseName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal server error processing request\"}");
        }
    }

    @GetMapping("/info")
    public ResponseEntity<String> getDiseaseInfoGet(@RequestParam("disease") String diseaseName , @RequestParam("city") String city) {
        if (diseaseName == null || diseaseName.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Missing 'disease' parameter in query parameter\"}");
        }
        try {
            String answer = qnaService.getAnswer(diseaseName,city);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            logger.error("Error processing GET request for disease: {}", diseaseName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal server error processing request\"}");
        }
    }
}
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class QuizGenerator {

    public static void main(String[] args) throws IOException {
        // data.json'dan verileri oku
        String jsonInput = Files.readString(Paths.get("data.json"));

        // JSON'u Liste formatına parse et
        List<Map<String, String>> inputList = parseJson(jsonInput);

        // Soru setini oluştur
        List<Map<String, Object>> quizData = generateQuiz(inputList);

        // JSON formatında `fragen.json` dosyasına yaz
        saveQuizToFile(quizData, "fragen.json");

        System.out.println("Soru seti fragen.json dosyasına kaydedildi.");
    }

    public static List<Map<String, Object>> generateQuiz(List<Map<String, String>> inputList) {
        List<Map<String, Object>> quizData = new ArrayList<>();

        for (Map<String, String> map : inputList) {
            String question = map.get("Türkisch");
            String correctAnswer = map.get("Deutsch");

            // Diğer yanlış seçenekleri seçmek için kullanılan set
            Set<String> optionsSet = new HashSet<>();
            optionsSet.add(correctAnswer);

            // Rastgele yanlış seçenekler seçiliyor
            while (optionsSet.size() < 4) {
                Map<String, String> randomEntry = inputList.get(RandomUtils.nextInt(0, inputList.size()));
                optionsSet.add(randomEntry.get("Deutsch"));
            }

            // Şıkları karıştır
            List<String> options = new ArrayList<>(optionsSet);
            Collections.shuffle(options);

            // Soru yapısını oluştur
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", question);
            questionData.put("options", options);
            questionData.put("answer", correctAnswer);

            quizData.add(questionData);
        }
        return quizData;
    }

    public static List<Map<String, String>> parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON'dan generic olmayan bir listeyi deserialize et
        return objectMapper.readValue(json, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    public static void saveQuizToFile(List<Map<String, Object>> quizData, String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Veriyi JSON formatında dosyaya yaz
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), quizData);
        } catch (IOException e) {
            System.err.println("Dosyaya yazma hatası: " + e.getMessage());
        }
    }
}

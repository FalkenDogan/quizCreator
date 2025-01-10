import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.RandomUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExcelQuizProcessor {

    public static void main(String[] args) {
        String excelFilePath = "excelData.xlsx"; // Excel dosya yolu
        String sheetName = "Sayfa1"; // Excel sheet adı

        // Excel'den verileri okuyup JSON'a dönüştür
        List<Map<String, String>> jsonData = convertExcelToJson(excelFilePath, sheetName);

        // data.json dosyasına yaz
        saveToJsonFile(jsonData, "data.json");
        System.out.println("Veriler data.json dosyasına kaydedildi.");

        // Soru setini oluştur
        List<Map<String, Object>> quizData = generateQuiz(jsonData);

        // fragen.json dosyasına yaz
        saveQuizToFile(quizData, "fragen.json");
        System.out.println("Soru seti fragen.json dosyasına kaydedildi.");
    }

    public static List<Map<String, String>> convertExcelToJson(String filePath, String sheetName) {
        List<Map<String, String>> resultList = new ArrayList<>();

        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            // Verilen sheet'e ulaş
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet " + sheetName + " not found in file: " + filePath);
            }

            // Satırları dolaş
            for (Row row : sheet) {
                Cell turkishCell = row.getCell(0); // A sütunu (Türkçe)
                Cell germanCell = row.getCell(1);  // B sütunu (Almanca)

                // Boş satırları veya sütunları kontrol et
                if (turkishCell == null || germanCell == null) {
                    continue;
                }

                String turkish = turkishCell.getStringCellValue();
                String german = germanCell.getStringCellValue();

                // Map oluştur ve listeye ekle
                Map<String, String> map = new HashMap<>();
                map.put("Türkisch", turkish);
                map.put("Deutsch", german);

                resultList.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public static void saveToJsonFile(List<Map<String, String>> data, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Daha okunaklı JSON için
        try {
            mapper.writeValue(new File(fileName), data);
        } catch (IOException e) {
            System.err.println("JSON dosyasına yazma hatası: " + e.getMessage());
        }
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


	import com.fasterxml.jackson.databind.ObjectMapper;
	import com.fasterxml.jackson.databind.SerializationFeature;
	import org.apache.poi.ss.usermodel.*;
	import org.apache.poi.xssf.usermodel.XSSFWorkbook;

	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileWriter;
	import java.io.IOException;
	import java.util.*;

	public class ExcelToJsonConverter {

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
	        System.out.println("resultList SIZE : " + resultList.size());
	        return resultList;
	    }

	    public static void main(String[] args) {
	        String filePath = "C:/Users/smust/Desktop/vocabulary.xlsx"; // Buraya dosya yolunu yaz
	        String sheetName = "Sayfa1";        // Buraya sheet adını yaz

	        // Metodu çağır
	        List<Map<String, String>> jsonData = convertExcelToJson(filePath, sheetName);

	        // JSON formatında çıktı
	        try {
	            ObjectMapper mapper = new ObjectMapper();
	            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Daha okunaklı JSON için

	            // JSON'u data.json dosyasına yaz
	            File outputFile = new File("data.json"); // Projenin ana dizinine yazılacak
	            mapper.writeValue(outputFile, jsonData);

	            System.out.println("JSON dosyaya yazıldı: " + outputFile.getAbsolutePath());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

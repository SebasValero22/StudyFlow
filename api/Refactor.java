import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Refactor {
    public static void main(String[] args) throws IOException {
        Path start = Paths.get("src/main/java/com/iescamp/studyflow_api");
        Files.walk(start)
             .filter(Files::isRegularFile)
             .filter(p -> p.toString().endsWith(".java"))
             .forEach(p -> {
                 try {
                     String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                     String newContent = content.replace("Subject", "LifeArea")
                                                .replace("subject", "lifeArea")
                                                .replace("nameLifeArea", "name")
                                                .replace("activeLifeArea", "active")
                                                .replace("/api/lifeAreas", "/api/subjects") // Keep endpoint same for frontends
                                                .replace("LifeAreas", "Subjects"); // For JSON keys maybe? 
                     
                     if (!content.equals(newContent)) {
                         Files.write(p, newContent.getBytes(StandardCharsets.UTF_8));
                         System.out.println("Refactored: " + p);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             });
    }
}

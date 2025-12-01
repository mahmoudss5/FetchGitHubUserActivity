import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter command (e.g., github-activity torvalds): ");
        String text = input.nextLine();

        String name;

        int idx= text.lastIndexOf(" ");
        if (idx != -1) {
            name = text.substring(idx + 1);
        } else {
            name = "";
        }

        if (name.length() < 1) {
            System.out.println("Error: Name is too short");
        } else {
            System.out.println("Target User: " + name);
            getResponse(name);
        }
    }

    public static void getResponse(String userName) {
        String link = "https://api.github.com/users/" + userName + "/events";
        System.out.println("Fetching data...");

        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                parser(content.toString());

            } else {
                System.out.println("Failed to connect. Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void parser(String text) {
        if (text.length() <= 2) {
            System.out.println("No Activity for this user");
            return;
        }

        text = text.replace("[", "");
        text = text.replace("]", "");

        String[] events = text.split("\\},\\{");

        System.out.println("--- Recent Activity ---");
        for (String event : events) {
            try {
                String type = getEventType(event);
                String repo = getRepoName(event);
                System.out.println("- " + type + " in " + repo);
            } catch (Exception e) {
                System.out.println("Error parsing event: " + e.getMessage());
            }
        }
    }


    public static String getEventType(String event) {
        int typeIdx = event.indexOf("\"type\":\"");
        if (typeIdx == -1) return "Unknown Event";

        int start = typeIdx + 8;
        int end = event.indexOf("\"", start);
        return event.substring(start, end);
    }

    public static String getRepoName(String event) {

        int repoKeyIdx = event.indexOf("\"repo\":");
        if (repoKeyIdx == -1) return "Unknown Repo";

        int nameKeyIdx = event.indexOf("\"name\":\"", repoKeyIdx);
        int start = nameKeyIdx + 8;
        int end = event.indexOf("\"", start);
        return event.substring(start, end);
    }
}
import javax.swing.*; //Imports Swing components for GUI (like JFrame, JLabel, JTextField, etc.)
import java.awt.*;//Provides layout managers and GUI components (FlowLayout, Color, etc.)
import java.awt.event.ActionEvent; //Used to handle events, especially button clicks.
import java.awt.event.ActionListener; 
import java.io.BufferedReader; //For reading the response from the weather API.
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection; //Used for networking: calling the OpenWeatherMap API.
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets; //For safely encoding the city name into the URL

public class WeatherAppGUI extends JFrame {
    String API_KEY = "6a709e943bf454649abaed82c30f710c"; // API key
    String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q="; //fetch weather data for given city

    JTextField cityField;
    JLabel resultLabel;
    JButton searchButton;

    public WeatherAppGUI() {
        setTitle("Weather App");
        ImageIcon i = new ImageIcon("weather.jpg");
        setIconImage(i.getImage());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(false);

        JLabel cityLabel = new JLabel("Enter City:");
        cityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cityField = new JTextField(15);
        JButton fetchButton = new JButton("Get Weather");
        resultLabel = new JLabel("Weather details will appear here...");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 15));

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText().trim();
                if (!city.isEmpty()) {
                    fetchWeather(city);
                } else {
                    resultLabel.setText("Please enter a city name.");
                }
            }
        });

        add(cityLabel);
        add(cityField);
        add(fetchButton);
        add(resultLabel);

        setVisible(true);
    }

    public void fetchWeather(String city) {
        try {
            //Encode city name for URL
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString()); //Safely encode the city name
            String urlString = BASE_URL + encodedCity + "&appid=" + API_KEY + "&units=metric";

            System.out.println("Fetching data from: " + urlString); 
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode); 

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                parseWeatherData(response.toString());
            } else {
                BufferedReader errorStream = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorStream.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorStream.close();
                System.out.println("Error Response: " + errorResponse.toString());
                resultLabel.setText("Error: City not found!");
            }
        } catch (java.net.UnknownHostException e) {
            // Handles no internet connection
            resultLabel.setText("No internet connection. Please check your network.");
        } catch (java.net.SocketTimeoutException e) {
            // Handles timeout issues
            resultLabel.setText("Request timed out. Please try again.");
        } catch (IOException e) {
            // General network error handling
            resultLabel.setText("Network error: " + e.getMessage());
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            resultLabel.setText("An unexpected error occurred: " + e.getMessage());
        }
    }

    void parseWeatherData(String response) {
        String cityName = extractValue(response, "\"name\":\"", "\"");
        String tempStr = extractValue(response, "\"temp\":", ",");
        String weatherDesc = extractValue(response, "\"description\":\"", "\"");

        resultLabel.setText(
            "<html><b>Weather in " + cityName + ":</b><br>🌡 " + tempStr + "°C<br>⛅ " + weatherDesc + "</html>"
        );
    }

    String extractValue(String json, String key, String endChar) {
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf(endChar, startIndex);
        return json.substring(startIndex, endIndex);
    }

    public static void main(String[] args) {
        new WeatherAppGUI();
    }
}

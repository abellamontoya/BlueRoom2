package com.example.blueroom;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SendGridHelper {

    private static final String SENDGRID_API_KEY = "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"; // Replace with your actual API key

    public static void sendEmail(String toEmail, String subject, String messageContent) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    Email from = new Email("theblueroomappshop@gmail.com");
                    Email to = new Email(toEmail);
                    Content content = new Content("text/plain", messageContent);
                    Mail mail = new Mail(from, subject, to, content);

                    Personalization personalization = new Personalization();
                    personalization.addTo(to);
                    mail.addPersonalization(personalization);

                    URL url = new URL("https://api.sendgrid.com/v3/mail/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + SENDGRID_API_KEY);
                    conn.setDoOutput(true);

                    String jsonInputString = mail.build();
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                        Log.d("SendGrid", "Email queued successfully");
                    } else {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        Log.e("SendGrid", "Error sending email: " + response.toString());
                    }
                } catch (Exception e) {
                    Log.e("SendGrid", "Error sending email", e);
                }
            }
        });

        thread.start();
    }
}
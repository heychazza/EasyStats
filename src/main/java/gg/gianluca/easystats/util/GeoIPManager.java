package gg.gianluca.easystats.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import gg.gianluca.easystats.EasyStats;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class GeoIPManager {
    private final EasyStats plugin;
    private DatabaseReader reader;
    private final Path databasePath;
    private static final String DOWNLOAD_URL = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key=%s&suffix=tar.gz";

    public GeoIPManager(EasyStats plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().toPath().resolve("GeoLite2-Country.mmdb");
    }

    public void initialize() {
        if (!Files.exists(databasePath)) {
            String licenseKey = plugin.getConfig().getString("maxmind.license-key");
            if (licenseKey == null || licenseKey.isEmpty()) {
                plugin.getLogger().warning("MaxMind license key not found in config.yml. GeoIP functionality will be disabled.");
                plugin.getLogger().warning("Get a free license key at https://www.maxmind.com/en/geolite2/signup");
                return;
            }
            downloadDatabase(licenseKey);
        }

        try {
            reader = new DatabaseReader.Builder(databasePath.toFile()).build();
            plugin.getLogger().info("GeoIP database loaded successfully!");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load GeoIP database", e);
        }
    }

    private void downloadDatabase(String licenseKey) {
        plugin.getLogger().info("Downloading GeoIP database...");
        String downloadUrl = String.format(DOWNLOAD_URL, licenseKey);

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                plugin.getLogger().severe("Failed to download GeoIP database. Response code: " + conn.getResponseCode());
                return;
            }

            // Create a temporary file for the downloaded .tar.gz
            Path tempFile = Files.createTempFile("geoip", ".tar.gz");
            
            // Download the file
            try (InputStream in = new GZIPInputStream(conn.getInputStream());
                 OutputStream out = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Extract the .mmdb file from the .tar.gz
            try (TarInputStream tarIn = new TarInputStream(Files.newInputStream(tempFile))) {
                TarEntry entry;
                while ((entry = tarIn.getNextEntry()) != null) {
                    if (entry.getName().endsWith("GeoLite2-Country.mmdb")) {
                        Files.copy(tarIn, databasePath, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }

            // Clean up the temporary file
            Files.delete(tempFile);
            plugin.getLogger().info("GeoIP database downloaded successfully!");

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to download GeoIP database", e);
        }
    }

    public String getCountry(InetAddress address) {
        if (reader == null) return "Unknown";
        
        try {
            CountryResponse response = reader.country(address);
            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get country for IP: " + address, e);
            return "Unknown";
        }
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close GeoIP database reader", e);
            }
        }
    }
} 
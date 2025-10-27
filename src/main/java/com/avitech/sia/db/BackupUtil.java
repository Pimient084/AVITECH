package com.avitech.sia.db;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupUtil {

    private static final String DB_NAME = "avicola2"; // Nombre de la base de datos actualizado
    private static final String DB_USER = "root";
    private static final String DB_PASS = "ca123";
    private static final String BACKUP_DIR = "C:/MySQLBackups";

    public static boolean createBackup() {
        // Asegúrate de que el directorio de respaldo exista
        File backupDirFile = new File(BACKUP_DIR);
        if (!backupDirFile.exists()) {
            backupDirFile.mkdirs();
        }

        // Genera un nombre de archivo único con la fecha y hora actual
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = DB_NAME + "_backup_" + timestamp + ".sql";
        String backupFilePath = BACKUP_DIR + File.separator + backupFileName;

        try {
            // Construye el comando mysqldump
            String mysqldumpPath = "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe"; // Ruta actualizada

            String[] command = {
                mysqldumpPath,
                "-u" + DB_USER,
                "-p" + DB_PASS,
                DB_NAME,
                "-r",
                backupFilePath
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Redirige el error stream al output stream

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Respaldo de la base de datos creado exitosamente en: " + backupFilePath);
                return true;
            } else {
                System.err.println("Error al crear el respaldo de la base de datos. Código de salida: " + exitCode);
                // Puedes leer el output del proceso para más detalles si es necesario
                // InputStream is = process.getInputStream();
                // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                // String line;
                // while ((line = reader.readLine()) != null) {
                //     System.err.println(line);
                // }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al ejecutar el comando mysqldump: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

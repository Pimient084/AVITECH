package com.avitech.sia.db;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupUtil {

    private static final String DB_NAME = "avicola2"; // Nombre de la base de datos actualizado
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123456";
    private static final String BACKUP_DIR = "C:/MySQLBackups";
    private static final String MYSQLDUMP_PATH = "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe";
    private static final String MYSQL_CLI_PATH = "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe";

    /** Devuelve el directorio donde se almacenan los respaldos */
    public static String getBackupDir() { return BACKUP_DIR; }

    /** Devuelve el nombre de la base de datos objetivo */
    public static String getDbName() { return DB_NAME; }

    /** Crea un respaldo y devuelve true/false (compatibilidad hacia atrás). */
    public static boolean createBackup() {
        return createBackupAndReturnFile() != null;
    }

    /**
     * Crea un respaldo y devuelve el archivo creado, o null si falla.
     */
    public static File createBackupAndReturnFile() {
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
            String[] command = new String[] {
                    MYSQLDUMP_PATH,
                    "-u" + DB_USER,
                    "-p" + DB_PASS,
                    DB_NAME,
                    "-r",
                    backupFilePath
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Respaldo de la base de datos creado exitosamente en: " + backupFilePath);
                return new File(backupFilePath);
            } else {
                System.err.println("Error al crear el respaldo de la base de datos. Código de salida: " + exitCode);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al ejecutar mysqldump: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** Lista los archivos de respaldo .sql ordenados por fecha de modificación descendente */
    public static File[] listBackups() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists() || !dir.isDirectory()) return new File[0];
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".sql"));
        if (files == null) return new File[0];
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files;
    }

    /**
     * Restaura la base de datos desde el archivo .sql indicado.
     * Nota: Requiere que el cliente mysql esté instalado en MYSQL_CLI_PATH.
     */
    public static boolean restoreBackup(File sqlFile) {
        Objects.requireNonNull(sqlFile, "sqlFile");
        if (!sqlFile.exists()) {
            System.err.println("El archivo a restaurar no existe: " + sqlFile.getAbsolutePath());
            return false;
        }
        try {
            // Usamos el cliente mysql con la opción -e "source <file>" para ejecutar el script
            String sourceCmd = "source \"" + sqlFile.getAbsolutePath().replace("\\", "/") + "\"";
            String[] command = new String[] {
                    "cmd.exe", "/c",
                    '"' + MYSQL_CLI_PATH + '"',
                    "-u" + DB_USER,
                    "-p" + DB_PASS,
                    DB_NAME,
                    "-e",
                    '"' + sourceCmd + '"'
            };
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Restauración completada desde: " + sqlFile.getAbsolutePath());
                return true;
            } else {
                System.err.println("Error al restaurar la base de datos. Código: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al restaurar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

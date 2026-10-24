
package school.sptech;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import school.sptech.config.DBConnectionProvider;
import school.sptech.config.Jira;
import school.sptech.config.S3Provider;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class ExemploCreateIssue {

    private static final String BUCKET_NAME = "biotrace-datalake";

    public static void main(String[] args) {
        try (S3Client s3Client = new S3Provider().getS3Client()) {

            listarObjetos(s3Client);
            baixarObjetos(s3Client);
            criarIssueNoJira();
            testarConexaoMySQL();

        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<S3Object> listarObjetos(S3Client s3Client) {
        List<S3Object> objetos = s3Client.listObjectsV2Paginator(
                request -> request.bucket(BUCKET_NAME)
        ).contents().stream().toList();

        System.out.println("Objetos encontrados no bucket:");

        for (S3Object objeto : objetos) {
            System.out.println("- " + objeto.key());
        }

        return objetos;
    }

    private static void baixarObjetos(S3Client s3Client) throws IOException {
        List<S3Object> objetos = listarObjetos(s3Client);
        Path diretorioBase = Path.of("downloads").toAbsolutePath().normalize();

        Files.createDirectories(diretorioBase);

        for (S3Object objeto : objetos) {
            Path destino = diretorioBase.resolve(objeto.key()).normalize();

            // Impede que uma chave saia da pasta de destino.
            if (!destino.startsWith(diretorioBase)) {
                throw new IOException("Caminho de arquivo inválido: " + objeto.key());
            }

            Files.createDirectories(destino.getParent());

            GetObjectRequest requisicao = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objeto.key())
                    .build();

            try (InputStream inputStream = s3Client.getObject(requisicao)) {
                Files.copy(
                        inputStream,
                        destino,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.out.println("Arquivo baixado: " + destino);
        }
    }



    private static void criarIssueNoJira()
            throws IOException, InterruptedException {

        String baseUrl = System.getenv("JIRA_BASE_URL");
        String email = System.getenv("JIRA_EMAIL");
        String apiToken = System.getenv("JIRA_API_TOKEN");

        Jira jira = new Jira(baseUrl, email, apiToken);

        String resposta = jira.createIssue(
                "AEIB",
                "Issue criada via Java",
                "Task"
        );

        System.out.println("Resposta do Jira: " + resposta);
    }

    private static void testarConexaoMySQL() {
        DBConnectionProvider provider = new DBConnectionProvider();
        JdbcTemplate jdbcTemplate = provider.getConnection();

        String versao = jdbcTemplate.queryForObject(
                "SELECT VERSION()",
                String.class
        );

        System.out.println("Conexão com MySQL realizada!");
        System.out.println("Versão: " + versao);
    }
}

package school.sptech.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class S3ObjectsService {

    private final S3Client s3Client;
    private final String bucketName;


    public S3ObjectsService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public List<S3Object> listarObjetos() {
        List<S3Object> objetos = s3Client.listObjectsV2Paginator(
                request -> request.bucket(bucketName)
        ).contents().stream().toList();

        System.out.println("Objetos encontrados no bucket:");

        for (S3Object objeto : objetos) {
            System.out.println("- " + objeto.key());
        }

        return objetos;
    }

    public void baixarObjetos() throws IOException {
        List<S3Object> objetos = listarObjetos();
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
                    .bucket(bucketName)
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
}

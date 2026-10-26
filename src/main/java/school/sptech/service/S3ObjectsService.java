package school.sptech.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class S3ObjectsService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3ObjectsService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public List<S3Object> listarObjetos(String prefixo) {
        List<S3Object> objetos = s3Client.listObjectsV2Paginator(
                        request -> request.bucket(bucketName).prefix(prefixo)
                ).contents().stream()
                .filter(o -> !o.key().endsWith("/"))
                .toList();

        System.out.println("Objetos encontrados em '" + prefixo + "':");
        for (S3Object objeto : objetos) {
            System.out.println("- " + objeto.key() + " (" + objeto.size() + " bytes)");
        }
        return objetos;
    }

    public List<Path> baixarObjetos(String prefixo) throws IOException {
        List<S3Object> objetos = listarObjetos(prefixo);
        List<Path> baixados = new ArrayList<>();

        Path diretorioBase = Path.of("downloads").toAbsolutePath().normalize();
        Files.createDirectories(diretorioBase);

        for (S3Object objeto : objetos) {
            Path destino = diretorioBase.resolve(objeto.key()).normalize();

            if (!destino.startsWith(diretorioBase)) {
                throw new IOException("Caminho de arquivo inválido: " + objeto.key());
            }

            Files.createDirectories(destino.getParent());

            GetObjectRequest requisicao = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objeto.key())
                    .build();

            try (InputStream in = s3Client.getObject(requisicao)) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            baixados.add(destino);
            System.out.println("Arquivo baixado: " + destino);
        }
        return baixados;
    }
}

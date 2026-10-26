package school.sptech;
import school.sptech.config.S3Provider;
import school.sptech.model.LeituraMaquina;
import school.sptech.model.LimiteMetrica;
import school.sptech.service.CsvReaderService;
import school.sptech.service.DatabaseService;
import school.sptech.service.JiraIssueService;
import school.sptech.service.S3ObjectsService;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ExemploCreateIssue {

    private static final String BUCKET_NAME = "biotrace-datalake";
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final JiraIssueService JIRA = new JiraIssueService();

    public static void main(String[] args) throws IOException, InterruptedException {

        DatabaseService db = new DatabaseService();
        S3Provider s3client = new S3Provider();
        S3ObjectsService s3 = new S3ObjectsService(s3client.getS3Client(), BUCKET_NAME);
        CsvReaderService csv = new CsvReaderService();

        Set<Path> processados = new HashSet<>();

        while (true) {
            List<Path> arquivos = s3.baixarObjetos("silver/");

            Optional<LimiteMetrica> cpu     = db.buscarPorTipoMetrica("cpu_percent");
            Optional<LimiteMetrica> memoria = db.buscarPorTipoMetrica("memoria_percent");
            Optional<LimiteMetrica> disco   = db.buscarPorTipoMetrica("disco_percent");
            Optional<LimiteMetrica> temperatura = db.buscarPorTipoMetrica("temperatura_cpu");

            for (Path arquivo : arquivos) {
                if (!processados.add(arquivo)) continue;

                List<LeituraMaquina> leituras = csv.ler(arquivo);
                for (LeituraMaquina l : leituras) {
                    verificar(l.numeroSerie(), "cpu_percent",     l.cpuPercent(),     l.dataHora(), cpu.orElse(null));
                    verificar(l.numeroSerie(), "memoria_percent", l.memoriaPercent(), l.dataHora(), memoria.orElse(null));
                    verificar(l.numeroSerie(), "disco_percent",   l.discoPercent(),   l.dataHora(), disco.orElse(null));
                    verificar(l.numeroSerie(), "temperatura_cpu",   l.temperaturaCpu(),   l.dataHora(), temperatura.orElse(null));

                }
            }

            Thread.sleep(300_000);
        }
    }

    private static void verificar(String serie, String tipoMetrica, Double valor,
                                  LocalDateTime dataHora, LimiteMetrica limite)
            throws IOException, InterruptedException {
        if (valor == null || limite == null) return;
        String horario = dataHora.format(FORMATO_DATA);

        if (limite.limiteUso() != null && valor > limite.limiteUso()) {
            JIRA.criarIssueNoJira(horario, serie, "ALERTA | " + tipoMetrica + " acima do uso");
            System.out.println(serie + " | " + tipoMetrica + " acima do uso: " + valor);
        }
        if (limite.limiteOciosidade() != null && valor < limite.limiteOciosidade()) {
            JIRA.criarIssueNoJira(horario, serie, "ALERTA | " + tipoMetrica + " ocioso");
            System.out.println(serie + " | " + tipoMetrica + " ocioso: " + valor);
        }
    }
}

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
import java.util.List;
import java.util.Optional;


public class ExemploCreateIssue {

    private static final String BUCKET_NAME = "biotrace-datalake";

    public static void main(String[] args) throws IOException, InterruptedException {

        DatabaseService db = new DatabaseService();
        JiraIssueService jira = new JiraIssueService();
        S3Provider s3client = new S3Provider();
        S3ObjectsService s3 = new S3ObjectsService(s3client.getS3Client(), BUCKET_NAME);

        s3.listarObjetos();
        s3.baixarObjetos();
        db.testarConexaoMySQL();

        CsvReaderService csv = new CsvReaderService();
        List<LeituraMaquina> leituras = csv.ler(Path.of("silver.csv"));
        Optional<LimiteMetrica> cpu = db.buscarPorTipoMetrica("cpu_percent");
        Optional<LimiteMetrica> memoria = db.buscarPorTipoMetrica("cpu_percent");
        Optional<LimiteMetrica> disco = db.buscarPorTipoMetrica("cpu_percent");

        for (LeituraMaquina l : leituras) {
            verificar(l.numeroSerie(), "cpu_percent",     l.cpuPercent(),  l.dataHora(),   cpu.orElse(null));
            verificar(l.numeroSerie(), "memoria_percent", l.memoriaPercent(), l.dataHora(), memoria.orElse(null));
            verificar(l.numeroSerie(), "disco_percent",   l.discoPercent(), l.dataHora(), disco.orElse(null));
        }

    }

    private static void verificar(String serie, String tipoMetrica, Double valor,LocalDateTime dataHora, LimiteMetrica limite) throws IOException, InterruptedException {
        if (valor == null || limite == null) return;
        JiraIssueService jira = new JiraIssueService();
        String horario = dataHora.format(FORMATO_DATA);

        if (limite.limiteUso() != null && valor > limite.limiteUso()) {
            String mensagem = "ALERTA | " + tipoMetrica + "acima do uso";
            jira.criarIssueNoJira(horario,serie,mensagem);
            System.out.println(serie + " | " + tipoMetrica + " acima do uso: " + valor);
        }
        if (limite.limiteOciosidade() != null && valor < limite.limiteOciosidade()) {
            String mensagem = "ALERTA | " + tipoMetrica + "ocioso";
            jira.criarIssueNoJira(horario,serie,mensagem);
            System.out.println(serie + " | " + tipoMetrica + " ocioso: " + valor);
        }
    }

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


}

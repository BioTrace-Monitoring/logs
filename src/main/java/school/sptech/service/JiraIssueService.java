package school.sptech.service;

import school.sptech.config.Jira;

import java.io.IOException;

public class JiraIssueService {

    public void criarIssueNoJira(String horario, String numeroSerie, String mensagem) throws IOException, InterruptedException {

        String baseUrl = System.getenv("JIRA_BASE_URL");
        String email = System.getenv("JIRA_EMAIL");
        String apiToken = System.getenv("JIRA_API_TOKEN");

        Jira jira = new Jira(baseUrl, email, apiToken);

        String titulo = "[" + numeroSerie + "] " + mensagem + " - " + horario;

        String resposta = jira.createIssue(
                "AEIB",
                titulo,
                "Task"
        );

        System.out.println("Resposta do Jira: " + resposta);
    }
}

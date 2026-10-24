package school.sptech.service;

import school.sptech.model.LeituraMaquina;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvReaderService {

    private static final char SEPARADOR = ';';
    private static final int TOTAL_COLUNAS = 15;
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public List<LeituraMaquina> ler(Path arquivo) throws IOException {
        List<LeituraMaquina> leituras = new ArrayList<>();

        try (BufferedReader entrada = new BufferedReader(
                new FileReader(arquivo.toFile(), StandardCharsets.UTF_8))) {

            entrada.readLine(); // descarta o cabeçalho

            String linha;
            while ((linha = entrada.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }

                List<String> colunas = lerColunas(linha);

                if (colunas.size() < TOTAL_COLUNAS) {
                    System.out.println("Linha ignorada (colunas insuficientes): " + linha);
                    continue;
                }

                leituras.add(converter(colunas));
            }
        }

        return leituras;
    }

    private LeituraMaquina converter(List<String> c) {
        return new LeituraMaquina(
                LocalDateTime.parse(c.get(0), FORMATO_DATA),
                c.get(1),
                toDouble(c.get(2)),
                toListaDouble(c.get(3)),
                toDouble(c.get(4)),
                toDouble(c.get(5)),
                toDouble(c.get(6)),
                toDouble(c.get(7)),
                toLong(c.get(8)),
                toLong(c.get(9)),
                toInt(c.get(10)),
                toInt(c.get(11)),
                toDouble(c.get(12)),
                toInt(c.get(13)),
                toDouble(c.get(14))
        );
    }

    private List<String> lerColunas(String linha) {
        List<String> colunas = new ArrayList<>();
        StringBuilder valorAtual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char caractere = linha.charAt(i);
            boolean proximaTambemAspas =
                    i + 1 < linha.length() && linha.charAt(i + 1) == '"';

            if (caractere == '"' && entreAspas && proximaTambemAspas) {
                valorAtual.append('"');
                i++;
            } else if (caractere == '"') {
                entreAspas = !entreAspas;
            } else if (caractere == SEPARADOR && !entreAspas) {
                colunas.add(valorAtual.toString());
                valorAtual.setLength(0);
            } else {
                valorAtual.append(caractere);
            }
        }

        colunas.add(valorAtual.toString());
        return colunas;
    }

    private Double toDouble(String s) {
        return s == null || s.isBlank() ? null : Double.valueOf(s.trim());
    }

    private Integer toInt(String s) {
        return s == null || s.isBlank() ? null : Integer.valueOf(s.trim());
    }

    private Long toLong(String s) {
        return s == null || s.isBlank() ? null : Long.valueOf(s.trim());
    }

    private List<Double> toListaDouble(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String limpo = s.replace("[", "").replace("]", "");
        List<Double> valores = new ArrayList<>();
        for (String parte : limpo.split(",")) {
            valores.add(Double.valueOf(parte.trim()));
        }
        return valores;
    }
}
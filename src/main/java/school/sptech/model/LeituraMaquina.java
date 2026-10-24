package school.sptech.model;

import java.time.LocalDateTime;
import java.util.List;

public record LeituraMaquina(
        LocalDateTime dataHora,
        String numeroSerie,
        Double cpuPercent,
        List<Double> cpuPorNucleo,
        Double memoriaPercent,
        Double memoriaDisponivelGb,
        Double discoPercent,
        Double discoLivreGb,
        Long redeBytesEnviados,
        Long redeBytesRecebidos,
        Integer redeErrosEntrada,
        Integer redeErrosSaida,
        Double uptimeHoras,
        Integer processosAtivos,
        Double temperaturaCpu
) {}
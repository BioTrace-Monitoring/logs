package school.sptech.service;

import org.springframework.jdbc.core.JdbcTemplate;
import school.sptech.config.DBConnectionProvider;
import school.sptech.model.LimiteMetrica;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseService() {
        this.jdbcTemplate = new DBConnectionProvider().getConnection();
    }

    public void testarConexaoMySQL() {
        String versao = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);

        System.out.println("Conexão com MySQL realizada!");
        System.out.println("Versão: " + versao);
    }

    public Optional<Double> buscarLimiteUso(String nomeComponente, String tipoMetrica) {
        String sql = """
                SELECT pm.limite_uso
                FROM parametro_metrica pm
                JOIN metrica m ON m.id_metrica = pm.fk_metrica
                JOIN componente c ON c.id_componente = m.fk_componente
                JOIN tipo_componente tc ON tc.id_tipo_componente = c.fk_tipo_componente
                JOIN tipo_metrica tm ON tm.id_tipo_metrica = m.fk_tipo_metrica
                WHERE tc.nome_componente = ?
                  AND tm.tipo_metrica = ?
                  AND pm.habilitado = 1
                  AND pm.limite_uso IS NOT NULL
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getDouble("limite_uso"),
                nomeComponente, tipoMetrica
        ).stream().findFirst();
    }

    public Optional<Double> buscarLimiteOciosidade(String nomeComponente, String tipoMetrica) {
        String sql = """
                SELECT pm.limite_ociosidade
                FROM parametro_metrica pm
                JOIN metrica m ON m.id_metrica = pm.fk_metrica
                JOIN componente c ON c.id_componente = m.fk_componente
                JOIN tipo_componente tc ON tc.id_tipo_componente = c.fk_tipo_componente
                JOIN tipo_metrica tm ON tm.id_tipo_metrica = m.fk_tipo_metrica
                WHERE tc.nome_componente = ?
                  AND tm.tipo_metrica = ?
                  AND pm.habilitado = 1
                  AND pm.limite_ociosidade IS NOT NULL
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getDouble("limite_ociosidade"),
                nomeComponente, tipoMetrica
        ).stream().findFirst();
    }

    public Optional<LimiteMetrica> buscarPorTipoMetrica(String tipoMetrica) {
        String sql = """
            SELECT tc.nome_componente, tm.tipo_metrica,
                   pm.limite_uso, pm.limite_ociosidade
            FROM parametro_metrica pm
            JOIN metrica m ON m.id_metrica = pm.fk_metrica
            JOIN componente c ON c.id_componente = m.fk_componente
            JOIN tipo_componente tc ON tc.id_tipo_componente = c.fk_tipo_componente
            JOIN tipo_metrica tm ON tm.id_tipo_metrica = m.fk_tipo_metrica
            WHERE tm.tipo_metrica = ?
              AND pm.habilitado = 1
            ORDER BY pm.id_parametro_metrica
            LIMIT 1
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LimiteMetrica(
                        rs.getString("nome_componente"),
                        rs.getString("tipo_metrica"),
                        rs.getObject("limite_uso", Double.class),
                        rs.getObject("limite_ociosidade", Double.class)
                ),
                tipoMetrica
        ).stream().findFirst();
    }
}


package school.sptech.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class DBConnectionProvider {

    private final DataSource dataSource;

    public DBConnectionProvider() {
        BasicDataSource basicDataSource = new BasicDataSource();

        final String mySqlUrl = System.getenv("MYSQL_URL");
        final String mySqlUser = System.getenv("MYSQL_USER");
        final String mySqlPassword = System.getenv("MYSQL_PSWD");

        basicDataSource.setUrl(mySqlUrl);
        basicDataSource.setUsername(mySqlUser);
        basicDataSource.setPassword(mySqlPassword);
        basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        this.dataSource = basicDataSource;
    }

    public JdbcTemplate getConnection() {
        return new JdbcTemplate(dataSource);
    }
}

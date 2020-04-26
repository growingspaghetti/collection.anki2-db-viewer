package com.growingspaghetti.anki.companion;

import com.growingspaghetti.anki.companion.model.Col;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

public class SqliteRepository {
  private final SqliteDbResolvable sqliteDbResolvable;
  private final QueryRunner runner = new QueryRunner();

  public SqliteRepository(SqliteDbResolvable sqliteDbResolvable) {
    this.sqliteDbResolvable = sqliteDbResolvable;
  }

  public Col fetchCol() throws Exception {
    try (Connection conn = getConnection(true)) {
      Map<String, Object> map = runner.query(conn, "SELECT * FROM col", new MapHandler());
      return KtObjectMapper.mapper.convertValue(map, Col.class);
    }
  }

  private Connection getConnection(boolean autoCommit) throws Exception {
    Class.forName("org.sqlite.JDBC");
    String dbPath = sqliteDbResolvable.getSqliteDb().getAbsolutePath();
    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    conn.setAutoCommit(autoCommit);
    return conn;
  }
}

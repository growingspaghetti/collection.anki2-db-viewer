package com.growingspaghetti.anki.companion;

import com.growingspaghetti.anki.companion.model.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqliteRepository {
  private final SqliteDbResolvable sqliteDbResolvable;
  private final QueryRunner runner = new QueryRunner();

  public SqliteRepository(SqliteDbResolvable sqliteDbResolvable) {
    this.sqliteDbResolvable = sqliteDbResolvable;
  }

  public Col fetchCol() throws Exception {
    try (Connection conn = getConnection(true)) {
      Map<String, Object> map = runner
          .query(conn, "SELECT * FROM col", new MapHandler());
      return KtObjectMapper.mapper.convertValue(map, Col.class);
    }
  }

  public List<Card> fetchCards() throws Exception {
    try (Connection conn = getConnection(true)) {
      List<Map<String, Object>> mapList = runner
          .query(conn, "SELECT * FROM cards", new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, Card.class))
          .collect(Collectors.toList());
    }
  }

  public List<Note> fetchNotes() throws Exception {
    try (Connection conn = getConnection(true)) {
      List<Map<String, Object>> mapList = runner
          .query(conn, "SELECT * FROM notes", new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, Note.class))
          .collect(Collectors.toList());
    }
  }

  public List<RevLog> fetchRevLogs() throws Exception {
    try (Connection conn = getConnection(true)) {
      List<Map<String, Object>> mapList = runner
          .query(conn, "SELECT * FROM revlog", new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, RevLog.class))
          .collect(Collectors.toList());
    }
  }

  public List<Grave> fetchGraves() throws Exception {
    try (Connection conn = getConnection(true)) {
      List<Map<String, Object>> mapList = runner
          .query(conn, "SELECT * FROM graves", new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, Grave.class))
          .collect(Collectors.toList());
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

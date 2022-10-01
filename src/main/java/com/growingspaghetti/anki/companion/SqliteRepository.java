package com.growingspaghetti.anki.companion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.growingspaghetti.anki.companion.model.Card;
import com.growingspaghetti.anki.companion.model.Col;
import com.growingspaghetti.anki.companion.model.Queue;
import com.growingspaghetti.anki.companion.model.RevLog;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
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

  // https://github.com/ankitects/anki/blob/master/pylib/anki/schedv2.py#L545
  public List<Card> queue(long deckId, Queue queue, long due) throws Exception {
    try (Connection conn = getConnection(true)) {
      String sql = "SELECT * FROM cards WHERE did = ? AND queue = ? AND due < ? ORDER BY due LIMIT 4096";
      List<Map<String, Object>> mapList = runner
          .query(conn, sql, new MapListHandler(), deckId, queue.getV(), due);
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, Card.class))
          .collect(Collectors.toList());
    }
  }

  public List<Card> queueReview(long deckId, Col col, int dayOffset) throws Exception {
    long millisSinceCreation = System.currentTimeMillis() - col.getCrt() * 1000;
    int daysSinceCreation = (int) (millisSinceCreation / ConstKt.ONE_DAY);
    return queue(deckId, Queue.REVIEW, daysSinceCreation + dayOffset);
  }

  public List<Card> queueDayRelearn(long deckId, Col col, int dayOffset) throws Exception {
    long millisSinceCreation = System.currentTimeMillis() - col.getCrt() * 1000;
    int daysSinceCreation = (int) (millisSinceCreation / ConstKt.ONE_DAY);
    return queue(deckId, Queue.DAY_RELEARN, daysSinceCreation + dayOffset);
  }

  public List<Card> queueLearning(long deckId, int dayOffset) throws Exception {
    long secTimestamp = (System.currentTimeMillis() + ConstKt.ONE_DAY * dayOffset) / 1000;
    return queue(deckId, Queue.RELEARN, secTimestamp);
  }

  public <T> List<T> findByIds(List<Long> ids, String tableName, Class<T> clazz) throws Exception {
    try (Connection conn = getConnection(true)) {
      String sql = String.format("SELECT * FROM %s WHERE id IN (%s)",
          tableName, ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
      List<Map<String, Object>> mapList = runner
          .query(conn, sql, new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, clazz))
          .collect(Collectors.toList());
    }
  }

  public List<RevLog> findRevLogsByCids(List<Long> ids) throws Exception {
    try (Connection conn = getConnection(true)) {
      String sql = String.format("SELECT * FROM revlog WHERE cid IN (%s)", ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
      List<Map<String, Object>> mapList = runner
          .query(conn, sql, new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, RevLog.class))
          .collect(Collectors.toList());
    }
  }

  public <T> T findById(String id, String tableName, Class<T> clazz) throws Exception {
    try (Connection conn = getConnection(true)) {
      String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);
      Map<String, Object> map = runner
          .query(conn, sql, new MapHandler(), id);
      return KtObjectMapper.mapper.convertValue(map, clazz);
    }
  }

  public <T> List<T> findAll(String tableName, Class<T> clazz) throws Exception {
    try (Connection conn = getConnection(true)) {
      String fields = Arrays.stream(clazz.getDeclaredFields())
          .map(Field::getName).collect(Collectors.joining(","));
      List<Map<String, Object>> mapList = runner
          .query(conn, String.format("SELECT %s FROM %s", fields, tableName), new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, clazz))
          .collect(Collectors.toList());
    }
  }

  public <T> void save(T o, String tableName) throws Exception {
    Map<String, Object> m = KtObjectMapper.mapper
        .convertValue(o, new TypeReference<Map<String, Object>>() {
        });
    String columns = String.join(",", m.keySet());
    String questions = String.join(",", Collections.nCopies(m.size(), "?"));
    try (Connection conn = getConnection(true)) {
      String sql = String.format("INSERT OR REPLACE INTO %s(%s) VALUES(%s)",
          tableName, columns, questions);
      runner.insert(conn, sql, new ScalarHandler<>(), m.values().toArray());
    }
  }

  public <T> void saveAll(List<T> objects, String tableName, Class<T> clazz) throws Exception {
    List<String> fields = Arrays.stream(clazz.getDeclaredFields())
        .map(Field::getName).collect(Collectors.toList());
    List<Map<String, Object>> mapList = KtObjectMapper.mapper
        .convertValue(objects, new TypeReference<List<Map<String, Object>>>() {
        });
    Object[][] matrix = mapList.stream()
        .map(m -> m.values().toArray()).toArray(Object[][]::new);
    String columns = String.join(",", fields);
    String questions = String.join(",", Collections.nCopies(fields.size(), "?"));
    try (Connection conn = getConnection(true)) {
      String sql = String.format("INSERT OR REPLACE INTO %s(%s) VALUES(%s)",
          tableName, columns, questions);
      runner.batch(conn, sql, matrix);
    }
  }

  public void delete(String id, String tableName) throws Exception {
    try (Connection conn = getConnection(true)) {
      String sql = String.format("DELETE FROM %s WHERE id = ?;", tableName);
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, id);
      ps.execute();
    }
  }

  public <T> List<T> fetchAll(String tableName, Class<T> clazz) throws Exception {
    try (Connection conn = getConnection(true)) {
      List<Map<String, Object>> mapList = runner
          .query(conn, "SELECT * FROM " + tableName, new MapListHandler());
      return mapList.stream()
          .map(m -> KtObjectMapper.mapper.convertValue(m, clazz))
          .collect(Collectors.toList());
    }
  }

  private Connection getConnection(boolean autoCommit) throws Exception {
    Class.forName("org.sqlite.JDBC");
    String dbPath = sqliteDbResolvable.sqliteDb().getAbsolutePath();
    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    conn.setAutoCommit(autoCommit);
    return conn;
  }
}

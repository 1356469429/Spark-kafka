package com.antrl.test
import com.spark.learn.test.core.{ParamFunSuite, SparkFunSuite}
import com.spark.sql.engine.SparksqlEngine
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.types.{StructType}
import org.apache.spark.sql.{Row}

/**
  * @author ${user.name}
  */
class App extends SparkFunSuite with ParamFunSuite {

  import spark.implicits._
  val sparkEngine = SparksqlEngine(spark)

  /**
    * 测试spark的sql功能
    */
  test("spark sql") {
    val schame = new StructType()
      .add("word", "string")
      .add("count", "int")
    val df = spark.createDataset(Seq(Row("word1", 1), Row("word2", 2)))(
      RowEncoder(schame))
    df.createOrReplaceTempView("lefttable")
    sparkEngine.sql(
      "CREATE OR REPLACE TEMPORARY VIEW fence_flow AS" +
        " select * from lefttable")
    sparkEngine.spark.table("fence_flow").show
  }

  /**
    *
    */
  test("ckp test") {
    sparkEngine.sql("checkpoints ab.`table` into 'hdfs:///ssxsxs/ssxxs'")
  }

  /**
    *
    */
  test("select hbase") {
    sparkEngine.sql(
      "select info(name1 string , name2 string),info3(name3 string , name4 string) FROM hbasetable where key='abc'")
  }

  /**
    *
    */
  test("hbase join") {
    val schame = new StructType()
      .add("word", "string")
      .add("count", "int")
    val df = spark.createDataset(Seq(Row("word1", 1), Row("word2", 2)))(
      RowEncoder(schame))
    df.createOrReplaceTempView("lefttable")
    sparkEngine
      .sql(
        s"""CREATE OR REPLACE TEMPORARY VIEW wordcountJoinHbaseTable AS
         | select word,count,info.ac FROM lefttable JOIN default:hbasetable
         |ON ROWKEY = word
         |CONF ZK = 'localhost:2181'""".stripMargin
      )
      .show

    sparkEngine
      .sql(
        "select word,count,info.ac FROM lefttable JOIN default:hbasetable" +
          " ON ROWKEY = word" +
          " CONF ZK = 'localhost:2181'")
      .show

  }

  test("udf test") {
    val schame = new StructType()
      .add("word", "string")
      .add("count", "int")
    val df = spark.createDataset(Seq(Row("word1", 1), Row("word2", 2)))(
      RowEncoder(schame))
    df.createOrReplaceTempView("lefttable")
    sparkEngine.register("testudf", (a: String) => {a + "ss"})
    sparkEngine.sql(s"""select testudf(word) as ne, count from lefttable""").show

  }

}

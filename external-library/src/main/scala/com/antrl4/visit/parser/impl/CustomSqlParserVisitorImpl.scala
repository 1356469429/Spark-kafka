package com.antrl4.visit.parser.impl

import com.antlr4.parser.{CustomSqlParserBaseVisitor, CustomSqlParserParser}
import com.antrl4.visit.operation.impl._
import scala.collection.JavaConverters._

class CustomSqlParserVisitorImpl
    extends CustomSqlParserBaseVisitor[AbstractVisitOperation] {

  override def visitHelloWordStatement(
      ctx: CustomSqlParserParser.HelloWordStatementContext)
    : AbstractVisitOperation = {
    HelloWordVisitOperation(ctx.word.getText)
  }

  override def visitCheckpointStatement(
      ctx: CustomSqlParserParser.CheckpointStatementContext)
    : AbstractVisitOperation = {
    CheckpointVisitOperation(ctx.table.getText, ctx.location.getText)
  }

  /**
    * 先进的这个，再进 @link  visitCheckpointStatement
    *
    * @param ctx the parse tree
   **/
  override def visitCheckpoint(
      ctx: CustomSqlParserParser.CheckpointContext): AbstractVisitOperation = {
    visitCheckpointStatement(ctx.checkpointStatement())
  }

  override def visitHelloWord(
      ctx: CustomSqlParserParser.HelloWordContext): AbstractVisitOperation = {
    visitHelloWordStatement(ctx.helloWordStatement())
  }

  /**
    *
    * @param ctx the parse tree
   **/
  override def visitSelectHbase(
      ctx: CustomSqlParserParser.SelectHbaseContext): AbstractVisitOperation = {
    val hbaseInfo = ctx.hBaseSearchState()
    val familyInfos = hbaseInfo.familyColumns.asScala.map(family => {
      val columnsInfo = family.columns.asScala.map(colms => {
        HbaseColumnsInfoOperation(colms.colName.getText, colms.colType.getText)
      })
      HbaseFamilyColumnsInfoOperation(family.familyName.getText, columnsInfo)
    })
    HbaseSearchInfoOperation(hbaseInfo.tableName.getText,
                             hbaseInfo.key.getText,
                             familyInfos)
  }

  /**
    *
    * @param ctx the parse tree
   **/
  override def visitHbaseJoin(
      ctx: CustomSqlParserParser.HbaseJoinContext): AbstractVisitOperation = {
    val hbasestate = ctx.hbaseJoinState()
    val cols = hbasestate.cols.asScala.map(x => {
      if (x.family == null)
        HbaseJoincolumn("", x.colname.getText)
      else {
        HbaseJoincolumn(x.family.getText, x.colname.getText)
      }
    })
    HbaseJoinInfoOperation(cols,
                           hbasestate.tablename.getText,
                           hbasestate.hbasetable.getText,
                           hbasestate.joinkey.getText,
                           hbasestate.zk.getText)
  }

}
package com.spark.sql.execution.impl

import com.antrl4.visit.operation.impl.ColumnsVisitOperationFactory.ColumnsWithUdfInfoOperation
import com.antrl4.visit.operation.impl.TableInfoVisitOperationFactory.TableSelectInfoOperation
import com.spark.sql.engine.common.UserDefinedFunction2
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.{StructField, StructType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class TableSelectExecutionImpl(
    df: Dataset[Row],
    info: TableSelectInfoOperation,
    udfManager: mutable.HashMap[String, UserDefinedFunction2])
    extends AbstractExecution {

  def exec(): DataFrame = {
    val schameMp = getSchameIndexMap(df)
    val newSchameMap = getNewSchame(info.columnsInfo, schameMp)(udfManager)

    df.mapPartitions(itor => {
      itor.map(r => {
        Row.fromSeq(newSchameMap.map {
          colInfo =>
            if (colInfo.rowIndex >= 0) {
              r.get(colInfo.rowIndex)
            } else {
              val udfParamV =
                colInfo.columnInfo.paramCols.map(x =>
                  r.get(schameMp(x.colName)._1))
                colInfo.userDefinedFunc.inputParamsNum match {
                  case 1 =>
                    val func = colInfo.userDefinedFunc.f.asInstanceOf[
                      Function1[Any, colInfo.userDefinedFunc.dataType.type]]
                    func(udfParamV.head)
                  case 2 =>
                    val func = colInfo.userDefinedFunc.f.asInstanceOf[
                      Function2[Any, Any, colInfo.userDefinedFunc.dataType.type]]
                    func(udfParamV(0), udfParamV(1))
                  case _ => throw new Exception("hhhh")
                }


            }
        })
      })
    })(RowEncoder(StructType(newSchameMap.map(_.structType))))

  }

}
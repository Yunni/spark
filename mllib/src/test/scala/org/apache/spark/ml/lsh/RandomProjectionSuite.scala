/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.ml.lsh

import org.apache.spark.SparkFunSuite
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.mllib.util.MLlibTestSparkContext

class RandomProjectionSuite extends SparkFunSuite with MLlibTestSparkContext {
  test("RandomProjection") {
    val data = {
      for (i <- -5 until 5; j <- -5 until 5) yield Vectors.dense(i.toDouble, j.toDouble)
    }
    val df = spark.createDataFrame(data.map(Tuple1.apply)).toDF("keys")

    // Project from 2 dimensional Euclidean Space to 1 dimensions
    val rp = new RandomProjection()
      .setOutputDim(1)
      .setInputCol("keys")
      .setOutputCol("values")
      .setBucketLength(1.0)

    val (falsePositive, falseNegative) = LSHTest.checkLSHProperty(df, rp, 8.0, 2.0)
    assert(falsePositive < 0.1)
    assert(falseNegative < 0.1)
  }

  test("RandomProjection with high dimension data") {
    val numDim = 100
    val data = {
      for (i <- 0 until numDim; j <- Seq(-2, -1, 1, 2))
        yield Vectors.sparse(numDim, Seq((i, j.toDouble)))
    }
    val df = spark.createDataFrame(data.map(Tuple1.apply)).toDF("keys")

    // Project from 100 dimensional Euclidean Space to 10 dimensions
    val rp = new RandomProjection()
      .setOutputDim(10)
      .setInputCol("keys")
      .setOutputCol("values")
      .setBucketLength(2.5)

    val (falsePositive, falseNegative) = LSHTest.checkLSHProperty(df, rp, 3.0, 2.0)
    assert(falsePositive < 0.1)
    assert(falseNegative < 0.1)
  }
}

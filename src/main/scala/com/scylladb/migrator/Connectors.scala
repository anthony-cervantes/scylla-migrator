package com.scylladb.migrator

import java.net.InetAddress

import com.datastax.spark.connector.cql.CassandraConnectorConf.CassandraSSLConf
import com.datastax.spark.connector.cql.{CassandraConnector, CassandraConnectorConf, NoAuthConf, PasswordAuthConf}
import com.scylladb.migrator.config.{Credentials, SourceSettings, TargetSettings}
import org.apache.spark.SparkConf

object Connectors {
  def sourceConnector(sparkConf: SparkConf, sourceSettings: SourceSettings.Cassandra) =
    new CassandraConnector(
      CassandraConnectorConf(sparkConf).copy(
        hosts = Set(InetAddress.getByName(sourceSettings.host)),
        port  = sourceSettings.port,
        authConf = sourceSettings.credentials match {
          case None                                  => NoAuthConf
          case Some(Credentials(username, password)) => PasswordAuthConf(username, password)
        },
        cassandraSSLConf = CassandraSSLConf(
         enabled = sparkConf.getBoolean("spark.scylladb.migrator.source.sslEnabled", false),
          enabledAlgorithms = sparkConf.getOption("spark.scylladb.migrator.source.sslAlgorithms").getOrElse("").split(",").toSet
        ),
        maxConnectionsPerExecutor = sourceSettings.connections,
        queryRetryCount           = -1
      )
    )

  def targetConnector(sparkConf: SparkConf, targetSettings: TargetSettings) =
    new CassandraConnector(
      CassandraConnectorConf(sparkConf).copy(
        hosts = Set(InetAddress.getByName(targetSettings.host)),
        port  = targetSettings.port,
        authConf = targetSettings.credentials match {
          case None                                  => NoAuthConf
          case Some(Credentials(username, password)) => PasswordAuthConf(username, password)
        },
        cassandraSSLConf = CassandraSSLConf(
          enabled = sparkConf.getBoolean("spark.scylladb.migrator.target.sslEnabled", false),
          enabledAlgorithms = sparkConf.getOption("spark.scylladb.migrator.target.sslAlgorithms").getOrElse("").split(",").toSet
        ),
        maxConnectionsPerExecutor = targetSettings.connections,
        queryRetryCount           = -1
      )
    )
}

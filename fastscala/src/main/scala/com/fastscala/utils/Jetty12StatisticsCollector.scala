package com.fastscala.utils

import io.prometheus.metrics.model.registry.{MultiCollector, PrometheusRegistry}
import io.prometheus.metrics.model.snapshots._
import org.eclipse.jetty.server.handler.StatisticsHandler

class Jetty12StatisticsCollector(val statisticsHandler: StatisticsHandler) extends MultiCollector {

  override def collect: MetricSnapshots = {
    val result = MetricSnapshots.builder()

    Seq(
      buildCounter("jetty_requests_total", "Number of requests", statisticsHandler.getRequestTotal),
      buildCounter("jetty_requests_failed_total", "Number of failed requests", statisticsHandler.getFailures),
      buildGauge("jetty_requests_active", "Number of requests currently active", statisticsHandler.getRequestsActive),
      buildGauge("jetty_requests_active_max", "Maximum number of requests that have been active at once", statisticsHandler.getRequestsActiveMax),
      buildGauge("jetty_request_time_max_seconds", "Maximum time spent in request handling", statisticsHandler.getRequestTimeMax / 1000.0),
      buildCounter("jetty_request_time_seconds_total", "Total time spent in all request handling", statisticsHandler.getRequestTimeTotal / 1000.0),
      buildGauge("jetty_request_time_seconds_mean", "Mean time spent in all request handling", statisticsHandler.getRequestTimeMean / 1000.0),
      buildGauge("jetty_request_time_seconds_stddev", "Standard deviation for time spent in all request handling", statisticsHandler.getRequestTimeStdDev),

      buildCounter("jetty_handler_total", "Number of calls to handle()", statisticsHandler.getHandleTotal),
      buildCounter("jetty_handler_failed_total", "Number of requests that throw an exception from handle()", statisticsHandler.getHandlingFailures),
      buildGauge("jetty_handler_active", "Number of requests currently in handle()", statisticsHandler.getHandleActive),
      buildGauge("jetty_handler_active_max", "Maximum number of requests in handle()", statisticsHandler.getHandleActiveMax),
      buildGauge("jetty_handler_time_max", "Maximum time spent in handle() execution", statisticsHandler.getHandleTimeMax / 1000.0),
      buildCounter("jetty_handler_time_seconds_total", "Total time spent in handle() execution", statisticsHandler.getHandleTimeTotal / 1000.0),
      buildGauge("jetty_handler_time_seconds_mean", "Mean time spent in handle() execution", statisticsHandler.getHandleTimeMean / 1000.0),
      buildGauge("jetty_handler_time_seconds_stddev", "Standard deviation for time spent in handle() execution", statisticsHandler.getHandleTimeStdDev),

      buildStatusCounter(),

      buildCounter("jetty_stats_seconds", "Time in seconds stats have been collected for", statisticsHandler.getStatisticsDuration.toSeconds / 1000.0),
      buildCounter("jetty_bytes_read_total", "Total count of bytes read", statisticsHandler.getBytesRead),
      buildCounter("jetty_bytes_written_total", "Total count of bytes written", statisticsHandler.getBytesWritten)
    ).foreach(result.metricSnapshot(_))

    result.build()
  }

  def buildGauge(name: String, help: String, value: Double): MetricSnapshot =
    GaugeSnapshot.builder()
      .name(PrometheusNaming.sanitizeMetricName(name))
      .help(help)
      .dataPoint(new GaugeSnapshot.GaugeDataPointSnapshot(value, Labels.EMPTY, null))
      .build()

  def buildCounter(name: String, help: String, value: Double): MetricSnapshot =
    CounterSnapshot.builder()
      .name(PrometheusNaming.sanitizeMetricName(name))
      .help(help)
      .dataPoint(new CounterSnapshot.CounterDataPointSnapshot(value, Labels.EMPTY, null, 0L))
      .build()

  def buildStatusCounter(): MetricSnapshot = {
    val name = "jetty_responses_total"
    val counter = CounterSnapshot.builder()
      .name(PrometheusNaming.sanitizeMetricName(name))
      .help("Number of requests with response status")

    Seq(
      buildStatusSample("1xx", statisticsHandler.getResponses1xx()),
      buildStatusSample("2xx", statisticsHandler.getResponses2xx()),
      buildStatusSample("3xx", statisticsHandler.getResponses3xx()),
      buildStatusSample("4xx", statisticsHandler.getResponses4xx()),
      buildStatusSample("5xx", statisticsHandler.getResponses5xx())
    ).foreach(counter.dataPoint(_))

    counter.build()
  }

  def buildStatusSample(status: String, value: Double): CounterSnapshot.CounterDataPointSnapshot =
    new CounterSnapshot.CounterDataPointSnapshot(value, Labels.of("code", status), null, 0L)

  def register() = PrometheusRegistry.defaultRegistry.register(this)
}

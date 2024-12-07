package com.fastscala.utils

import io.prometheus.metrics.model.registry.{MultiCollector, PrometheusRegistry}
import io.prometheus.metrics.model.snapshots.{Unit as PUnit, *}
import org.eclipse.jetty.server.handler.StatisticsHandler

class Jetty12StatisticsCollector(val statisticsHandler: StatisticsHandler) extends MultiCollector {

  override def collect: MetricSnapshots = {
    val result = MetricSnapshots.builder()

    Seq(
      buildCounter("jetty_requests_total", "Number of requests", statisticsHandler.getRequestTotal),
      buildCounter("jetty_requests_failed_total", "Number of failed requests", statisticsHandler.getFailures),
      buildGauge("jetty_requests_active", "Number of requests currently active", statisticsHandler.getRequestsActive),
      buildGauge("jetty_requests_active_max", "Maximum number of requests that have been active at once", statisticsHandler.getRequestsActiveMax),
      buildGauge("jetty_request_time_max_seconds", "Maximum time spent in request handling", PUnit.nanosToSeconds(statisticsHandler.getRequestTimeMax), PUnit.SECONDS),
      buildCounter("jetty_request_time_seconds_total", "Total time spent in all request handling", PUnit.nanosToSeconds(statisticsHandler.getRequestTimeTotal), PUnit.SECONDS),
      buildGauge("jetty_request_time_mean_seconds", "Mean time spent in all request handling", statisticsHandler.getRequestTimeMean / 1e9, PUnit.SECONDS),
      buildGauge("jetty_request_time_stddev_seconds", "Standard deviation for time spent in all request handling", statisticsHandler.getRequestTimeStdDev / 1e9, PUnit.SECONDS),

      buildCounter("jetty_handler_total", "Number of calls to handle()", statisticsHandler.getHandleTotal),
      buildCounter("jetty_handler_failed_total", "Number of requests that throw an exception from handle()", statisticsHandler.getHandlingFailures),
      buildGauge("jetty_handler_active", "Number of requests currently in handle()", statisticsHandler.getHandleActive),
      buildGauge("jetty_handler_active_max", "Maximum number of requests in handle()", statisticsHandler.getHandleActiveMax),
      buildGauge("jetty_handler_time_max_seconds", "Maximum time spent in handle() execution", PUnit.nanosToSeconds(statisticsHandler.getHandleTimeMax), PUnit.SECONDS),
      buildCounter("jetty_handler_time_seconds_total", "Total time spent in handle() execution", PUnit.nanosToSeconds(statisticsHandler.getHandleTimeTotal), PUnit.SECONDS),
      buildGauge("jetty_handler_time_mean_seconds", "Mean time spent in handle() execution", statisticsHandler.getHandleTimeMean / 1e9, PUnit.SECONDS),
      buildGauge("jetty_handler_time_stddev_seconds", "Standard deviation for time spent in handle() execution", statisticsHandler.getHandleTimeStdDev / 1e9, PUnit.SECONDS),

      buildStatusCounter(),

      buildCounter("jetty_stats_duration_seconds_total", "Time stats have been collected for", statisticsHandler.getStatisticsDuration.toSeconds, PUnit.SECONDS),
      buildCounter("jetty_read_bytes_total", "Total count of bytes read", statisticsHandler.getBytesRead, PUnit.BYTES),
      buildCounter("jetty_written_bytes_total", "Total count of bytes written", statisticsHandler.getBytesWritten, PUnit.BYTES)
    ).foreach(result.metricSnapshot(_))

    result.build()
  }

  def buildGauge(name: String, help: String, value: Double): MetricSnapshot = buildGauge(name, help, value, null)

  def buildGauge(name: String, help: String, value: Double, unit: PUnit): MetricSnapshot =
    GaugeSnapshot.builder()
      .name(PrometheusNaming.sanitizeMetricName(name))
      .help(help)
      .unit(unit)
      .dataPoint(new GaugeSnapshot.GaugeDataPointSnapshot(value, Labels.EMPTY, null))
      .build()

  def buildCounter(name: String, help: String, value: Double): MetricSnapshot = buildCounter(name, help, value, null)

  def buildCounter(name: String, help: String, value: Double, unit: PUnit): MetricSnapshot =
    CounterSnapshot.builder()
      .name(PrometheusNaming.sanitizeMetricName(name))
      .help(help)
      .unit(unit)
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

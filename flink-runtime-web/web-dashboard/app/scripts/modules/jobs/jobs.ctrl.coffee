#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

angular.module('flinkApp')

.controller 'RunningJobsController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.runningJobs = []
  $scope.completedJobs = []

  loadJobs = ->
    JobsService.loadJobs().then (data) ->
      $scope.runningJobs = []
      $scope.completedJobs = []

      # collect the jobs
      (data.jobs).forEach (job, i) ->
        switch job.status
          when 'FINISHED'
            $scope.completedJobs.push job
          when 'CANCELED'
            $scope.completedJobs.push job
          when 'FAILED'
            $scope.completedJobs.push job
          else
            $scope.runningJobs.push job

  loadJobs()

  refresh = $interval ->
      loadJobs()
    , flinkConfig["refresh-interval"]

    $scope.$on '$destroy', ->
      $interval.cancel(refresh)

# --------------------------------------

.controller 'CompletedJobsController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.runningJobs = []
  $scope.completedJobs = []

  loadJobs = ->
    JobsService.loadJobs().then (data) ->
      $scope.runningJobs = []
      $scope.completedJobs = []

      # collect the jobs
      (data.jobs).forEach (job, i) ->
        switch job.status
          when 'FINISHED'
            $scope.completedJobs.push job
          when 'CANCELED'
            $scope.completedJobs.push job
          when 'FAILED'
            $scope.completedJobs.push job
          else
            $scope.runningJobs.push job

  loadJobs()

  refresh = $interval ->
      loadJobs()
    , flinkConfig["refresh-interval"]

    $scope.$on '$destroy', ->
      $interval.cancel(refresh)

# --------------------------------------

.controller 'SingleJobController', ($scope, $state, $stateParams, JobsService, MetricsService, $rootScope, flinkConfig, $interval, $q, watermarksConfig) ->
  $scope.id = $stateParams.jobid
  $scope.name = null
  $scope.startTime = null
  $scope.endTime = null
  $scope.duration = null
  $scope.status = null
  $scope.vertices = null
  $scope.plan = null
  $scope.aggs = ["max", "min", "sum", "avg"]

  loadJob = ->
    JobsService.loadJob($scope.id).then (data) ->
      $scope.name = data.name
      $scope.startTime = data.startTime
      $scope.endTime = data.endTime
      $scope.duration = data.duration
      $scope.status = data.status
      $scope.vertices = data.vertices
      $scope.plan = data.plan

      $scope.$broadcast 'reload'

  loadJob()

  refresh = $interval ->
    loadJob()

  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'JobOverviewController', ($scope, $state, $stateParams, $window, JobsService) ->
  return

# --------------------------------------

.controller 'JobOverviewVerticesController', ($scope, JobsService) ->
  return

# --------------------------------------

.controller 'JobOverviewRecordsController', ($scope, JobsService) ->
  return

# --------------------------------------

.controller 'JobOverviewBytesController', ($scope, JobsService) ->
  return

# --------------------------------------

.controller 'JobCheckpointsController', ($scope, $state, $stateParams) ->
  $scope.checkpointId = null

  $scope.$watch($scope.checkpointId, () ->
    console.log("checkpointId changed", $scope.checkpointId)

  )

  return

# --------------------------------------

.controller 'JobCheckpointsOverviewController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.lastCompletedCheckpoint = null
  $scope.checkpoints = []
  $scope.checkpointCounts = []
  $scope.checkpointLabels = ["PENDING", "COMPLETED", "FAILED"]
  $scope.checkpointColors = ["#428cba", "#5cb85c", "#d9534f"]
  $scope.chartOptions = {
    legend: {
      display: true,
      position: "right"
    }
  }
  $scope.order = "-id"
  $scope.page = 1
  $scope.limit = 5

  JobsService.getCheckpointConfig($scope.id).then (data) ->
    $scope.checkpointConfig = data

  loadCheckpoints = ->
    JobsService.loadCheckpoints($scope.id).then (data) ->
      $scope.checkpointCounts = [
        data.numPendingCheckpoints,
        data.numCompletedCheckpoints,
        data.numFailedCheckpoints
      ]

      $scope.lastCompletedCheckpoint = data.lastCompletedCheckpoint
      $scope.checkpoints = data.checkpoints

  loadCheckpoints()

  refresh = $interval ->
    loadCheckpoints()
  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'JobCheckpointsConfigController', ($scope, $state, $stateParams, JobsService) ->
  # Request the config once (it's static)
  JobsService.getCheckpointConfig($scope.id).then (data) ->
    $scope.checkpointConfig = data

# --------------------------------------

.controller 'JobCheckpointsCheckpointDetailController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.checkpointId = $stateParams.checkpointid
  $scope.vertexInfos = new Map()
  $scope.vertices = []
  $scope.durationAggs = ["max", "min", "avg"]
  $scope.sizeAggs = ["max", "min", "avg", "sum"]

  loadCheckpoint = ->
    JobsService.loadCheckpoint($scope.id, $scope.checkpointId).then (data) ->
      $scope.vertices = data.vertices

      ($scope.vertices).forEach (vertex) ->
        vertex.name = $scope.vertexInfos.get(vertex.vertexId)

  JobsService.loadJob($scope.id).then (data) ->
    (data.vertices).forEach (vertex) ->
      $scope.vertexInfos.set(vertex.id, vertex.name)

    loadCheckpoint()

    refresh = $interval ->
      loadCheckpoint()

    , flinkConfig["refresh-interval"]

    $scope.$on '$destroy', ->
      $interval.cancel(refresh)

  return

# --------------------------------------

.controller 'JobCheckpointsVertexDetailController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.checkpointId = $stateParams.checkpointid
  $scope.vertexId = $stateParams.vertexid
  $scope.tasks = []
  $scope.limit = 5
  $scope.page = 1

  JobsService.loadVertex($scope.id, $scope.vertexId).then (data) ->
    $scope.vertexName = data.name

  loadCheckpointVertex = ->
    JobsService.loadCheckpointVertex($scope.id, $scope.checkpointId, $scope.vertexId).then (data) ->
      $scope.tasks = data.tasks

  loadCheckpointVertex()

  refresh = $interval ->
    loadCheckpointVertex()

  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'JobExceptionsController', ($scope, $state, $stateParams, $interval, flinkConfig, JobsService) ->
  $scope.exceptions = []
  $scope.page = 1
  $scope.limit = 5
  $scope.order = "-time"

  loadExceptions = ->
    JobsService.loadExceptions($scope.id).then (data) ->
      $scope.exceptions = data.exceptions

  loadExceptions()

  refresh = $interval ->
    loadExceptions()

  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'SingleVertexController', ($scope, $state, $stateParams, JobsService, MetricsService, $rootScope, flinkConfig, $interval, $q, watermarksConfig) ->
  $scope.jobId = $stateParams.jobid
  $scope.id = $stateParams.vertexid
  $scope.jobName = null
  $scope.name = null
  $scope.numTasksPerState = {}
  $scope.tasks = null

  JobsService.loadJob($scope.jobId).then (data) ->
    $scope.jobName = data.name

  loadVertex = ->
    JobsService.loadVertex($scope.jobId, $scope.id).then (data) ->
      $scope.name = data["name"]
      $scope.tasks = data["tasks"]

      $scope.numTasksPerState = {
        "DEPLOYING": 0,
        "FAILED": 0,
        "RECONCILING": 0,
        "RUNNING": 0,
        "CREATED": 0,
        "SCHEDULED": 0,
        "FINISHED": 0,
        "CANCELED": 0,
        "CANCELING": 0
      }

      ($scope.tasks).forEach (task) ->
        $scope.numTasksPerState[task.currentExecution.status]++

      $scope.$broadcast 'reload'

  loadVertex()

  refresh = $interval ->
    loadVertex()

  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'VertexTasksController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  return

# --------------------------------------

.controller 'VertexRecordsController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  return

# --------------------------------------

.controller 'VertexBytesController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  return

# --------------------------------------

.controller 'SingleTaskController', ($scope, $state, $stateParams, JobsService, MetricsService, $rootScope, flinkConfig, $interval, $q, watermarksConfig) ->
  $scope.jobId = $stateParams.jobid
  $scope.vertexId = $stateParams.vertexid
  $scope.index = $stateParams.taskindex
  $scope.jobName = "N/A"
  $scope.vertexName = "N/A"
  $scope.executions = null

  JobsService.loadJob($scope.jobId).then (data) ->
    $scope.jobName = data.name

  JobsService.loadVertex($scope.jobId, $scope.vertexId).then (data) ->
    $scope.vertexName = data.name

  loadTask = ->
    JobsService.loadTask($scope.jobId, $scope.vertexId, $scope.index).then (data) ->
      $scope.executions = data["executions"]

      $scope.$broadcast 'reload'

  loadTask()

  refresh = $interval ->
    loadTask()

  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)

# --------------------------------------

.controller 'TaskExecutionsController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  $scope.myOrder="-attemptNumber"
  return

# --------------------------------------

.controller 'TaskRecordsController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  return

# --------------------------------------

.controller 'TaskBytesController', ($scope, JobsService) ->
  $scope.myPage = 1
  $scope.myLimit = 10
  return

# --------------------------------------

.controller 'JobPlanAccumulatorsController', ($scope, JobsService) ->
  getAccumulators = ->
    JobsService.getAccumulators($scope.nodeid).then (data) ->
      $scope.accumulators = data.main
      $scope.subtaskAccumulators = data.subtasks

  if $scope.nodeid and (!$scope.vertex or !$scope.vertex.accumulators)
    getAccumulators()

  $scope.$on 'reload', (event) ->
    getAccumulators() if $scope.nodeid

# --------------------------------------

.controller 'JobTimelineVertexController', ($scope, $state, $stateParams, JobsService) ->
  getVertex = ->
    JobsService.getVertex($stateParams.vertexId).then (data) ->
      $scope.vertex = data

  getVertex()

  $scope.$on 'reload', (event) ->
    getVertex()

# --------------------------------------

.controller 'JobPropertiesController', ($scope, JobsService) ->
  $scope.changeNode = (nodeid) ->
    if nodeid != $scope.nodeid
      $scope.nodeid = nodeid

      JobsService.getNode(nodeid).then (data) ->
        $scope.node = data

    else
      $scope.nodeid = null
      $scope.node = null

# --------------------------------------

.controller 'JobPlanMetricsController', ($scope, JobsService, MetricsService) ->
  $scope.dragging = false
  $scope.window = MetricsService.getWindow()
  $scope.availableMetrics = null

  $scope.$on '$destroy', ->
    MetricsService.unRegisterObserver()

  loadMetrics = ->
    JobsService.getVertex($scope.nodeid).then (data) ->
      $scope.vertex = data

    MetricsService.getAvailableMetrics($scope.jobid, $scope.nodeid).then (data) ->
      $scope.availableMetrics = data.sort(alphabeticalSortById)
      $scope.metrics = MetricsService.getMetricsSetup($scope.jobid, $scope.nodeid).names

      MetricsService.registerObserver($scope.jobid, $scope.nodeid, (data) ->
        $scope.$broadcast "metrics:data:update", data.timestamp, data.values
      )

  alphabeticalSortById = (a, b) ->
    A = a.id.toLowerCase()
    B = b.id.toLowerCase()
    if A < B
      return -1
    else if A > B
      return 1
    else
      return 0

  $scope.dropped = (event, index, item, external, type) ->

    MetricsService.orderMetrics($scope.jobid, $scope.nodeid, item, index)
    $scope.$broadcast "metrics:refresh", item
    loadMetrics()
    false

  $scope.dragStart = ->
    $scope.dragging = true

  $scope.dragEnd = ->
    $scope.dragging = false

  $scope.addMetric = (metric) ->
    MetricsService.addMetric($scope.jobid, $scope.nodeid, metric.id)
    loadMetrics()

  $scope.removeMetric = (metric) ->
    MetricsService.removeMetric($scope.jobid, $scope.nodeid, metric)
    loadMetrics()

  $scope.setMetricSize = (metric, size) ->
    MetricsService.setMetricSize($scope.jobid, $scope.nodeid, metric, size)
    loadMetrics()

  $scope.setMetricView = (metric, view) ->
    MetricsService.setMetricView($scope.jobid, $scope.nodeid, metric, view)
    loadMetrics()

  $scope.getValues = (metric) ->
    MetricsService.getValues($scope.jobid, $scope.nodeid, metric)

  $scope.$on 'node:change', (event, nodeid) ->
    loadMetrics() if !$scope.dragging

  loadMetrics() if $scope.nodeid

# --------------------------------------

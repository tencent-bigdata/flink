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

angular.module('flinkApp', ['ui.router', 'angularMoment', 'dndLists', 'ngMaterial', 'md.data.table', 'chart.js'])

# --------------------------------------

.run ($rootScope) ->
  $rootScope.sidebarVisible = false
  $rootScope.showSidebar = ->
    $rootScope.sidebarVisible = !$rootScope.sidebarVisible
    $rootScope.sidebarClass = 'force-show'

# --------------------------------------

.value 'flinkConfig', {
  jobServer: ''
#  jobServer: 'http://localhost:8081/'
  "refresh-interval": 10000
}

# --------------------------------------

.value 'watermarksConfig', {
  # A value of (Java) Long.MIN_VALUE indicates that there is no watermark
  # available. This is parsed by Javascript as this number. We have it as
  # a constant here to compare available watermarks against.
  noWatermark: -9223372036854776000
}

# --------------------------------------

.config ($uiViewScrollProvider) ->
  $uiViewScrollProvider.useAnchorScroll()

# --------------------------------------

.run ($rootScope, $state) ->
  $rootScope.$on '$stateChangeStart', (event, toState, toParams, fromState) ->
    if toState.redirectTo
      event.preventDefault()
      $state.go toState.redirectTo, toParams

# --------------------------------------

.config ($stateProvider, $urlRouterProvider) ->
  $stateProvider.state "overview",
    url: "/overview"
    views:
      main:
        templateUrl: "partials/overview.html"
        controller: 'OverviewController'

  .state "running-jobs",
    url: "/running-jobs"
    views:
      main:
        templateUrl: "partials/jobs/running-jobs.html"
        controller: 'RunningJobsController'

  .state "completed-jobs",
    url: "/completed-jobs"
    views:
      main:
        templateUrl: "partials/jobs/completed-jobs.html"
        controller: 'CompletedJobsController'

  .state "single-job",
    url: "/jobs/{jobid}"
    abstract: true
    views:
      main:
        templateUrl: "partials/jobs/job.html"
        controller: 'SingleJobController'

  .state "single-job.overview",
    url: ""
    redirectTo: "single-job.overview.vertices"
    views:
      details:
        templateUrl: "partials/jobs/job.overview.html"
        controller: 'JobOverviewController'

  .state "single-job.overview.vertices",
    url: ""
    views:
      'node-details':
        templateUrl: "partials/jobs/job.overview.vertices.html"
        controller: 'JobOverviewVerticesController'

  .state "single-job.overview.records",
    url: ""
    views:
      'node-details':
        templateUrl: "partials/jobs/job.overview.records.html"
        controller: 'JobOverviewRecordsController'

  .state "single-job.overview.bytes",
    url: ""
    views:
      'node-details':
        templateUrl: "partials/jobs/job.overview.bytes.html"
        controller: 'JobOverviewBytesController'

  .state "single-job.checkpoints",
    url: "/checkpoints"
    redirectTo: "single-job.checkpoints.main.overview"
    views:
      details:
        templateUrl: "partials/jobs/job.checkpoints.html"
        controller: 'JobCheckpointsController'

  .state "single-job.checkpoints.main",
    url: ""
    redirectTo: "single-job.checkpoints.main.overview"
    views:
      'checkpoints':
        templateUrl: "partials/jobs/job.checkpoints.main.html"

  .state "single-job.checkpoints.main.config",
    url: ""
    views:
      'node-details':
        templateUrl: "partials/jobs/job.checkpoints.config.html"
        controller: 'JobCheckpointsConfigController'

  .state "single-job.checkpoints.main.overview",
    url: ""
    views:
      'node-details':
        templateUrl: "partials/jobs/job.checkpoints.overview.html"
        controller: 'JobCheckpointsOverviewController'

  .state "single-job.checkpoints.checkpoint-detail",
    url: "/{checkpointid}"
    views:
      'checkpoints':
        templateUrl: "partials/jobs/job.checkpoints.checkpoint-detail.html"
        controller: 'JobCheckpointsCheckpointDetailController'

  .state "single-job.checkpoints.vertex-detail",
    url: "/{checkpointid}/vertices/{vertexid}"
    views:
      'checkpoints':
        templateUrl: "partials/jobs/job.checkpoints.vertex-detail.html"
        controller: 'JobCheckpointsVertexDetailController'

  .state "single-job.exceptions",
    url: "/exceptions"
    views:
      details:
        templateUrl: "partials/jobs/job.exceptions.html"
        controller: 'JobExceptionsController'

  .state "single-vertex",
    url: "/jobs/{jobid}/vertices/{vertexid}"
    redirectTo: "single-vertex.tasks"
    views:
      main:
        templateUrl: "partials/jobs/vertex.html"
        controller: 'SingleVertexController'

  .state "single-vertex.tasks",
    url: ""
    views:
      'details':
        templateUrl: "partials/jobs/vertex.tasks.html"
        controller: 'VertexTasksController'

  .state "single-vertex.records",
    url: ""
    views:
      details:
        templateUrl: "partials/jobs/vertex.records.html"
        controller: 'VertexRecordsController'

  .state "single-vertex.bytes",
    url: ""
    views:
      details:
        templateUrl: "partials/jobs/vertex.bytes.html"
        controller: 'VertexBytesController'

  .state "single-task",
    url: "/jobs/{jobid}/vertices/{vertexid}/tasks/{taskindex}"
    redirectTo: "single-task.executions"
    views:
      main:
        templateUrl: "partials/jobs/task.html"
        controller: 'SingleTaskController'

  .state "single-task.executions",
    url: ""
    views:
      'details':
        templateUrl: "partials/jobs/task.executions.html"
        controller: 'TaskExecutionsController'

  .state "single-task.records",
    url: ""
    views:
      details:
        templateUrl: "partials/jobs/task.records.html"
        controller: 'TaskRecordsController'

  .state "single-task.bytes",
    url: ""
    views:
      details:
        templateUrl: "partials/jobs/task.bytes.html"
        controller: 'TaskBytesController'

  .state "single-job.overview.accumulators",
    url: "/accumulators"
    views:
      'node-details':
        templateUrl: "partials/jobs/job.plan.node-list.accumulators.html"
        controller: 'JobPlanAccumulatorsController'

  .state "single-job.plan.checkpoints.details",
    url: "/details/{checkpointId}"
    views:
      'checkpoints-view':
        templateUrl: "partials/jobs/job.plan.node.checkpoints.details.html"
        controller: 'JobPlanCheckpointDetailsController'

  .state "single-job.timeline",
    url: "/timeline"
    views:
      details:
        templateUrl: "partials/jobs/job.timeline.html"

  .state "single-job.timeline.vertex",
    url: "/{vertexId}"
    views:
      vertex:
        templateUrl: "partials/jobs/job.timeline.vertex.html"
        controller: 'JobTimelineVertexController'

  .state "single-job.config",
    url: "/config"
    views:
      details:
        templateUrl: "partials/jobs/job.config.html"

  .state "all-manager",
    url: "/taskmanagers"
    views:
      main:
        templateUrl: "partials/taskmanager/index.html"
        controller: 'AllTaskManagersController'

  .state "single-manager",
      url: "/taskmanager/{taskmanagerid}"
      abstract: true
      views:
        main:
          templateUrl: "partials/taskmanager/taskmanager.html"
          controller: 'SingleTaskManagerController'

  .state "single-manager.metrics",
    url: "/metrics"
    views:
      details:
        templateUrl: "partials/taskmanager/taskmanager.metrics.html"

  .state "single-manager.stdout",
    url: "/stdout"
    views:
      details:
        templateUrl: "partials/taskmanager/taskmanager.stdout.html"
        controller: 'SingleTaskManagerStdoutController'

  .state "single-manager.log",
    url: "/log"
    views:
      details:
        templateUrl: "partials/taskmanager/taskmanager.log.html"
        controller: 'SingleTaskManagerLogsController'

  .state "jobmanager",
      url: "/jobmanager"
      views:
        main:
          templateUrl: "partials/jobmanager/index.html"

  .state "jobmanager.config",
    url: "/config"
    views:
      details:
        templateUrl: "partials/jobmanager/config.html"
        controller: 'JobManagerConfigController'

  .state "jobmanager.stdout",
    url: "/stdout"
    views:
      details:
        templateUrl: "partials/jobmanager/stdout.html"
        controller: 'JobManagerStdoutController'

  .state "jobmanager.log",
    url: "/log"
    views:
      details:
        templateUrl: "partials/jobmanager/log.html"
        controller: 'JobManagerLogsController'

  .state "submit",
      url: "/submit"
      views:
        main:
          templateUrl: "partials/submit.html"
          controller: "JobSubmitController"

  $urlRouterProvider.otherwise "/overview"

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

.controller 'OverviewController', ($scope, OverviewService, JobsService, TaskManagersService, $interval, flinkConfig) ->
  $scope.flinkVersion = "N/A"
  $scope.flinkCommit = "N/A"

  $scope.runningJobs = []
  $scope.completedJobs = []
  $scope.jobLabels = ["RUNNING", "FINISHED", "CANCELED", "FAILED"]
  $scope.jobColors = ["#428cba", "#5cb85c", "#5bc0de", "#d9534f"]
  $scope.jobCounts = []
  $scope.chartOptions = {
    legend: {
      display: true,
      position: "right"
    }
  }

  $scope.taskManagers = []
  $scope.numTaskManagers = -1
  $scope.cpu = -1
  $scope.memory = -1
  $scope.numSlots = -1
  $scope.numAvailableSlots = -1

  OverviewService.loadInfo().then (data) ->
    $scope.flinkVersion = data.version
    $scope.flinkCommit = data.commit

  loadJobs = ->
    JobsService.loadJobs().then (data) ->
      $scope.runningJobs = []
      $scope.completedJobs = []

      $scope.jobCounts = [
        data.numRunningJobs,
        data.numFinishedJobs,
        data.numCanceledJobs,
        data.numFailedJobs
      ]

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

  loadTaskManagers = ->
    TaskManagersService.loadTaskManagers().then (data) ->
      $scope.taskManagers = data.taskManagers
      $scope.numTaskManagers = 0
      $scope.numSlots = 0
      $scope.numAvailableSlots = 0
      $scope.cpu = 0
      $scope.memory = 0
      ($scope.taskManagers).forEach (taskManager) ->
        $scope.numTaskManagers++
        $scope.numSlots += taskManager.slotsNumber
        $scope.numAvailableSlots += taskManager.freeSlots
        $scope.cpu += taskManager.hardware.cpuCores
        $scope.memory += taskManager.hardware.physicalMemory

  loadJobs()
  loadTaskManagers()

  refresh = $interval ->
    loadJobs()
    loadTaskManagers()
  , flinkConfig["refresh-interval"]

  $scope.$on '$destroy', ->
    $interval.cancel(refresh)
